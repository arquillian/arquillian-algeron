package org.arquillian.algeron.pact.provider.core;

import au.com.dius.pact.model.Consumer;
import au.com.dius.pact.model.Pact;
import au.com.dius.pact.model.ProviderState;
import au.com.dius.pact.model.RequestResponseInteraction;
import au.com.dius.pact.model.RequestResponsePact;
import org.apache.http.HttpRequest;
import org.arquillian.algeron.pact.provider.api.Pacts;
import org.arquillian.algeron.pact.provider.spi.ArquillianTestClassAwareTarget;
import org.arquillian.algeron.pact.provider.spi.CurrentConsumer;
import org.arquillian.algeron.pact.provider.spi.CurrentInteraction;
import org.arquillian.algeron.pact.provider.spi.PactProviderExecutionAwareTarget;
import org.arquillian.algeron.pact.provider.spi.ProviderContextAwareTarget;
import org.arquillian.algeron.pact.provider.spi.State;
import org.arquillian.algeron.pact.provider.spi.Target;
import org.arquillian.algeron.pact.provider.spi.TargetRequestFilter;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Runner that will execute the same test for all defined Pacts
 */
public class InteractionRunner {

    private Logger logger = Logger.getLogger(InteractionRunner.class.getName());

    @Inject
    Instance<Pacts> pactsInstance;

    @Inject
    Instance<Target> targetInstance;

    public void executePacts(@Observes EventContext<Test> test) {
        final Pacts pacts = pactsInstance.get();
        if (pacts == null) {
            test.proceed();
            return;
        }

        TestClass testClass = test.getEvent().getTestClass();

        final List<Throwable> errors = new ArrayList<>();
        validateState(testClass, errors);
        validateTargetRequestFilters(testClass, errors);
        validateTestTarget(testClass, errors);

        Field interactionField =
            validateAndGetResourceField(testClass, RequestResponseInteraction.class, CurrentInteraction.class, errors);
        Field consumerField = validateAndGetResourceField(testClass, Consumer.class, CurrentConsumer.class, errors);

        if (errors.size() != 0) {
            String errorMessage = errors.stream()
                .map(Throwable::getMessage)
                .collect(Collectors.joining(" * "));
            throw new IllegalArgumentException(errorMessage);
        }

        executePacts(test, pacts, interactionField, consumerField);
    }

    private void executePacts(EventContext<Test> test, final Pacts pacts, final Field interactionField,
        final Field consumerField) {
        final TestClass testClass = test.getEvent().getTestClass();
        final Object testInstance = test.getEvent().getTestInstance();

        for (Pact pact : pacts.getPacts()) {
            RequestResponsePact requestResponsePact = (RequestResponsePact) pact;

            // Inject current consumer
            if (consumerField != null) {
                setField(testInstance, consumerField, pact.getConsumer());
            }

            for (final RequestResponseInteraction interaction : requestResponsePact.getInteractions()) {
                Map<String, ?> stateParams = executeStateChanges(interaction, testClass, testInstance);

                Target target = targetInstance.get();

                if (target instanceof ArquillianTestClassAwareTarget) {
                    ArquillianTestClassAwareTarget arquillianTestClassAwareTarget =
                        (ArquillianTestClassAwareTarget) target;
                    arquillianTestClassAwareTarget.setTestClass(testClass, testInstance);
                }

                if (target instanceof PactProviderExecutionAwareTarget) {
                    PactProviderExecutionAwareTarget pactProviderExecutionAwareTarget =
                        (PactProviderExecutionAwareTarget) target;
                    pactProviderExecutionAwareTarget.setConsumer(pact.getConsumer());
                    pactProviderExecutionAwareTarget.setRequestResponseInteraction(interaction);
                }
                if (target instanceof ProviderContextAwareTarget) {
                    ProviderContextAwareTarget providerContextAwareTarget = (ProviderContextAwareTarget) target;
                    providerContextAwareTarget.setStateParams(stateParams);
                }

                // Inject current interaction to test
                if (interactionField != null) {
                    setField(testInstance, interactionField, interaction);
                }

                // run the test
                test.proceed();
            }
        }
    }

    private void setField(Object testInstance, Field fieldTarget, Object pact) {
        try {
            fieldTarget.setAccessible(true);
            fieldTarget.set(testInstance, pact);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void validateState(final TestClass testClass, final List<Throwable> errors) {
        Arrays.stream(testClass.getMethods(State.class)).forEach(method -> validatePublicVoidMethods(method, errors));
    }

    private void validateTargetRequestFilters(final TestClass testClass, final List<Throwable> errors) {
        Method[] methods = testClass.getMethods(TargetRequestFilter.class);
        for (Method method : methods) {
            if (!isPublic(method)) {
                String publicError = String.format("Method %s annotated with %s should be public.", method.getName(),
                    TargetRequestFilter.class.getName());
                logger.log(Level.SEVERE, publicError);
                errors.add(new IllegalArgumentException(publicError));
            }

            if (method.getParameterCount() != 1) {
                String argumentError = String.format("Method %s should take only a single %s parameter", method.getName(),
                    HttpRequest.class.getName());
                logger.log(Level.SEVERE, argumentError);
                errors.add(new IllegalArgumentException(argumentError));
            } else if (!HttpRequest.class.isAssignableFrom(method.getParameterTypes()[0])) {
                String httpRequestError = String.format("Method %s should take only %s parameter", method.getName(),
                    HttpRequest.class.getName());
                logger.log(Level.SEVERE, httpRequestError);
                errors.add(new IllegalArgumentException(httpRequestError));
            }
        }
    }

    private void validateTestTarget(TestClass testClass, final List<Throwable> errors) {
        final List<Field> fieldsWithAnnotation =
            getFieldsWithAnnotation(testClass.getJavaClass(), ArquillianResource.class)
                .stream()
                .filter(f -> Target.class.isAssignableFrom(f.getType()))
                .collect(Collectors.toList());
        if (fieldsWithAnnotation.size() > 1) {
            final String testTargetError = String.format("Test should have one field annotated with %s of type %s",
                ArquillianResource.class.getName(), Target.class.getName());
            logger.log(Level.SEVERE, testTargetError);
            errors.add(new IllegalArgumentException(testTargetError));
        } else if (fieldsWithAnnotation.size() == 0) {
            final String testTargetError =
                String.format("Field annotated with %s should implement %s and didn't found any",
                    ArquillianResource.class.getName(), Target.class.getName());
            logger.log(Level.SEVERE, testTargetError);
            errors.add(new IllegalArgumentException(testTargetError));
        }
    }

    private void validatePublicVoidMethods(Method method, final List<Throwable> errors) {
        if (!isPublic(method)) {
            String publicError = String.format("Method %s should be public.", method.getName());
            logger.log(Level.SEVERE, publicError);
            errors.add(new IllegalArgumentException(publicError));
        }
        if (!(returnsVoid(method) || returnsMap(method))) {
            String voidError = "Method %s should return void or Map";
            logger.log(Level.SEVERE, voidError);
            errors.add(new IllegalArgumentException(voidError));
        }
    }

    private Field validateAndGetResourceField(TestClass testClass, Class<?> fieldType,
        Class<? extends Annotation> annotation, List<Throwable> errors) {
        final List<Field> fieldsWithArquillianResource = getFieldsWithAnnotation(testClass.getJavaClass(), annotation);

        List<Field> rri = fieldsWithArquillianResource
            .stream()
            .filter(
                field -> fieldType.isAssignableFrom(field.getType())
            ).collect(Collectors.toList());

        if (rri.size() > 1) {
            String rriError =
                String.format("Only one field annotated with %s of type %s should be present", annotation.getName(),
                    fieldType.getName());
            logger.log(Level.SEVERE, rriError);
            errors.add(new IllegalArgumentException(rriError));
        } else {
            if (rri.size() == 1) {
                return rri.get(0);
            }
        }

        return null;
    }

    protected Map<String, ?> executeStateChanges(final RequestResponseInteraction interaction, final TestClass testClass,
        final Object target) {
        Map<String, ?> statesParams = new HashMap<>();
        if (!interaction.getProviderStates().isEmpty()) {
            for (final ProviderState state : interaction.getProviderStates()) {
                Arrays.stream(testClass.getMethods(State.class))
                    .filter(method -> ArrayMatcher.matches(
                        method.getAnnotation(State.class).value(), state.getName()))
                    .forEach(method -> {
                        Object stateParams;
                        if (isStateMethodWithMapParameter(method)) {
                            stateParams = executeMethod(method, target, state.getParams());
                        } else {
                            if (method.getParameterCount() > 0) {
                                // Use regular expressions to pass parameters.
                                stateParams = executeStateMethodWithRegExp(method, state, target);
                            } else {
                                stateParams = executeMethod(method, target);
                            }
                        }
                        if (stateParams instanceof Map) {
                            statesParams.putAll((Map)stateParams);
                        }
                    });
            }
        }
        return statesParams;
    }

    private Object executeStateMethodWithRegExp(Method stateMethod, ProviderState state, Object target) {
        final Optional<String> matchingStateOptional = ArrayMatcher.firstMatch(
            stateMethod.getAnnotation(State.class).value(), state.getName());
        // We are sure that at this point, an state is present
        final String matchingState = matchingStateOptional.get();
        final List<String> arguments = ArgumentPatternMatcher.arguments(
            Pattern.compile(matchingState), state.getName());

        if (arguments.size() != stateMethod.getParameterCount()) {
            throw new IllegalArgumentException(String.format(
                "Consumer state %s matches with provider state %s but provider method contains %s arguments instead of matching %s",
                state.getName(), matchingState, stateMethod.getParameterCount(), arguments.size()));
        }

        Class<?>[] parameterTypes = stateMethod.getParameterTypes();

        final Object[] instances = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameter = parameterTypes[i];
            instances[i] = StateTypeConverter.convert(arguments.get(i), parameter);
        }

        return executeMethod(stateMethod, target, instances);
    }

    private boolean isStateMethodWithMapParameter(Method method) {
        return method.getParameterCount() == 1 &&
            Map.class.isAssignableFrom(method.getParameterTypes()[0]);
    }

    private Object executeMethod(Method method, Object target, Object... params) {
        try {
            return method.invoke(target, params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private List<Field> getFieldsWithAnnotation(final Class<?> source,
        final Class<? extends Annotation> annotationClass) {
        List<Field> declaredAccessableFields = AccessController
            .doPrivileged((PrivilegedAction<List<Field>>) () -> {
                List<Field> foundFields = new ArrayList<>();
                Class<?> nextSource = source;
                while (nextSource != Object.class) {
                    for (Field field : nextSource.getDeclaredFields()) {
                        if (field.isAnnotationPresent(annotationClass)) {
                            if (!field.isAccessible()) {
                                field.setAccessible(true);
                            }
                            foundFields.add(field);
                        }
                    }
                    nextSource = nextSource.getSuperclass();
                }
                return foundFields;
            });
        return declaredAccessableFields;
    }

    private boolean isPublic(Method method) {
        return Modifier.isPublic(method.getModifiers());
    }

    private boolean returnsVoid(Method method) {
        return Void.TYPE.equals(method.getReturnType());
    }

    private boolean returnsMap(Method method) {
        return Map.class.equals(method.getReturnType());
    }
}
