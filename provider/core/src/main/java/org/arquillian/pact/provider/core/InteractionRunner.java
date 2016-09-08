package org.arquillian.pact.provider.core;

import au.com.dius.pact.model.Consumer;
import au.com.dius.pact.model.Pact;
import au.com.dius.pact.model.RequestResponseInteraction;
import au.com.dius.pact.model.RequestResponsePact;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.TargetRequestFilter;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestClassAwareTarget;
import au.com.dius.pact.provider.junit.target.TestTarget;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpRequest;
import org.arquillian.pact.provider.api.Pacts;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Runner that will execute the same test for all defined Pacts
 */
public class InteractionRunner {

    private Logger logger = Logger.getLogger(InteractionRunner.class.getName());

    @Inject
    Instance<Pacts> pactsInstance;

    public void executePacts(@Observes EventContext<Test> test) {
        TestClass testClass = test.getEvent().getTestClass();

        final List<Throwable> errors = new ArrayList<>();
        validatePublicVoidNoArgMethods(testClass, State.class, errors);
        validateTestTarget(testClass, errors);
        validateTargetRequestFilters(testClass, errors);

        Field interactionField = validateAndGetArquillianResourceField(testClass, RequestResponseInteraction.class, errors);
        Field consumerField = validateAndGetArquillianResourceField(testClass, Consumer.class, errors);

        if (errors.size() != 0) {
            String errorMessage = errors.stream()
                    .map(error -> error.getMessage())
                    .collect(Collectors.joining(" * "));
            throw new IllegalArgumentException(errorMessage);
        }

        Pacts pacts = pactsInstance.get();
        if (pacts != null) {
            executePacts(test, pacts, interactionField, consumerField);
        } else {
            logger.log(Level.WARNING, "No pacts read for execution");
        }

    }

    private void executePacts(EventContext<Test> test, final Pacts pacts, final Field interactionField, final Field consumerField) {
        final TestClass testClass = test.getEvent().getTestClass();
        final Object testInstance = test.getEvent().getTestInstance();

        for (Pact pact : pacts.getPacts()) {
            RequestResponsePact requestResponsePact = (RequestResponsePact) pact;

            // Inject current consumer
            setField(testInstance, consumerField, pact.getConsumer());
            for (final RequestResponseInteraction interaction : requestResponsePact.getInteractions()) {
                executeStateChanges(interaction, testClass, testInstance);

                Target target = getTarget(testClass, testInstance);
                if (target instanceof TestClassAwareTarget) {
                    logger.log(Level.SEVERE, String.format("Targets implementing %s are not supported in Arquillian extension", TestClassAwareTarget.class.getName()));
                }

                // Inject current interaction to test
                setField(testInstance, interactionField, interaction);

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

    private Target getTarget(TestClass testClass, Object testInstance) {
        Field fieldTarget = getFieldsWithAnnotation(testClass.getJavaClass(), TestTarget.class).get(0);
        Target target;
        try {
            fieldTarget.setAccessible(true);
            target = (Target) fieldTarget.get(testInstance);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
        return target;
    }

    protected void validateTargetRequestFilters(final TestClass testClass, final List<Throwable> errors) {
        Method[] methods = testClass.getMethods(TargetRequestFilter.class);
        for (Method method : methods) {
            if (!isPublic(method)) {
                String publicError = String.format("Method %s annotated with %s should be public.", method.getName(), TargetRequestFilter.class.getName());
                logger.log(Level.SEVERE, publicError);
                errors.add(new IllegalArgumentException(publicError));
            }

            if (method.getParameterCount() != 1) {
                String argumentError = String.format("Method %s should take only a single %s parameter", method.getName(), HttpRequest.class.getName());
                logger.log(Level.SEVERE, argumentError);
                errors.add(new IllegalArgumentException(argumentError));
            } else if (!HttpRequest.class.isAssignableFrom(method.getParameterTypes()[0])) {
                String httpRequestError = String.format("Method %s should take only %s parameter", method.getName(), HttpRequest.class.getName());
                logger.log(Level.SEVERE, httpRequestError);
                errors.add(new IllegalArgumentException(httpRequestError));
            }
        }
    }

    protected void validatePublicVoidNoArgMethods(final TestClass testClass, final Class<? extends Annotation> annotation, final List<Throwable> errors) {
        Method[] methods = testClass.getMethods(annotation);
        for (Method method : methods) {
            if (!isPublic(method)) {
                String publicError = String.format("Method %s annotated with %s should be public.", method.getName(), annotation.getName());
                logger.log(Level.SEVERE, publicError);
                errors.add(new IllegalArgumentException(publicError));
            }
            if (method.getParameterCount() != 0) {
                String parametersError = String.format("Method %s annotated with %s should contain no parameters", method.getName(), annotation.getName());
                logger.log(Level.SEVERE, parametersError);
            }
        }
    }

    protected void validateTestTarget(final TestClass testClass, List<Throwable> errors) {
        List<Field> annotatedFields = getFieldsWithAnnotation(testClass.getJavaClass(), TestTarget.class);
        if (annotatedFields.size() != 1) {
            String moreThanOneTestTargetError = String.format("Test class should have exactly one field annotated with %s", TestTarget.class.getName());
            logger.log(Level.SEVERE, moreThanOneTestTargetError);
            errors.add(new IllegalArgumentException(moreThanOneTestTargetError));
        } else if (!Target.class.isAssignableFrom(annotatedFields.get(0).getType())) {
            String implementationInterface = String.format("Field annotated with %s should implement %s interface", TestTarget.class.getName(), Target.class.getName());
            logger.log(Level.SEVERE, implementationInterface);
            errors.add(new IllegalArgumentException(implementationInterface));
        }
    }

    private Field validateAndGetArquillianResourceField(TestClass testClass, Class<?> fieldType, List<Throwable> errors) {
        final List<Field> fieldsWithArquillianResource = getFieldsWithAnnotation(testClass.getJavaClass(), ArquillianResource.class);

        List<Field> rri = fieldsWithArquillianResource
                .stream()
                .filter(
                        field -> fieldType.isAssignableFrom(field.getType())
                ).collect(Collectors.toList());

        if (rri.size() != 1) {
            String rriError = String.format("Only one field annotated with %s of type %s should be present", ArquillianResource.class.getName(),fieldType.getName());
            logger.log(Level.SEVERE, rriError);
            errors.add(new IllegalArgumentException(rriError));
        } else {
            return rri.get(0);
        }

        return null;

    }

    protected void executeStateChanges(final RequestResponseInteraction interaction, final TestClass testClass, final Object target) {
        if (interaction.getProviderState() != null && !interaction.getProviderState().isEmpty()) {
            final String state = interaction.getProviderState();
            for (Method ann: testClass.getMethods(State.class)) {
                if (ArrayUtils.contains(ann.getAnnotation(State.class).value(), state)) {
                    try {
                        ann.invoke(target);
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException(e);
                    } catch (InvocationTargetException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }

        }
    }


    private List<Field> getFieldsWithAnnotation(final Class<?> source,
                                                      final Class<? extends Annotation> annotationClass) {
        List<Field> declaredAccessableFields = AccessController
                .doPrivileged(new PrivilegedAction<List<Field>>() {
                    public List<Field> run() {
                        List<Field> foundFields = new ArrayList<Field>();
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
                    }
                });
        return declaredAccessableFields;
    }
    private boolean isPublic(Method method) {
        return Modifier.isPublic(method.getModifiers());
    }
}
