package org.arquillian.pact.consumer.core;

import au.com.dius.pact.consumer.ConsumerPactBuilder;
import au.com.dius.pact.consumer.PactError;
import au.com.dius.pact.consumer.PactMismatch;
import au.com.dius.pact.consumer.PactVerified$;
import au.com.dius.pact.consumer.UserCodeFailed;
import au.com.dius.pact.consumer.VerificationResult;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.MockProviderConfig;
import au.com.dius.pact.model.PactFragment;
import org.arquillian.pact.consumer.core.client.container.ConsumerProviderPair;
import org.arquillian.pact.consumer.core.util.ResolveClassAnnotation;
import org.arquillian.pact.consumer.spi.Pact;
import org.arquillian.pact.consumer.spi.PactVerification;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public abstract class AbstractConsumerPactTest {

    private static final Logger logger = Logger.getLogger(AbstractConsumerPactTest.class.getName());
    private static final VerificationResult PACT_VERIFIED = PactVerified$.MODULE$;

    @Inject
    protected Instance<PactConsumerConfiguration> pactConsumerConfigurationInstance;

    @Inject
    protected Instance<MockProviderConfig> mockProviderConfigInstance;

    protected ConsumerProviderPair executeConsumerTest(EventContext<Test> testEventContext, TestClass testClass, PactVerification pactVerification) throws Throwable {
        String currentProvider = getProvider(testClass, pactVerification);

        // Start of execution

        return executePactFragment(testEventContext, currentProvider, pactVerification);
    }

    private ConsumerProviderPair executePactFragment(EventContext<Test> testEventContext, String currentProvider, PactVerification pactVerification) throws Throwable {
        final Object testInstance = testEventContext.getEvent().getTestInstance();
        final TestClass testClass = testEventContext.getEvent().getTestClass();

        Optional<Method> possiblePactMethod = findPactMethod(currentProvider, testClass, pactVerification);
        if (!possiblePactMethod.isPresent()) {
            throw new UnsupportedOperationException("Could not find method with @Pact for the provider " + currentProvider);
        }

        Method method = possiblePactMethod.get();
        Pact pact = method.getAnnotation(Pact.class);
        PactDslWithProvider dslBuilder = ConsumerPactBuilder.consumer(pact.consumer()).hasPactWith(currentProvider);
        PactFragment pactFragment;

        try {
            pactFragment = (PactFragment) method.invoke(testInstance, dslBuilder);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke pact method", e);
        }

        VerificationResult result = runPactTest(testEventContext, pactFragment);
        validateResult(result, pactVerification);

        return new ConsumerProviderPair(pact.consumer(), currentProvider);
    }

    private VerificationResult runPactTest(EventContext<Test> base, PactFragment pactFragment) {
        return pactFragment.runConsumer(mockProviderConfigInstance.get(), mockProviderConfig -> base.proceed());
    }

    private void validateResult(VerificationResult result, PactVerification pactVerification) throws Throwable {
        if (!result.equals(PACT_VERIFIED)) {
            if (result instanceof PactError) {
                throw ((PactError) result).error();
            }
            if (result instanceof UserCodeFailed) {
                throw ((UserCodeFailed<RuntimeException>) result).error();
            }
            if (result instanceof PactMismatch) {
                PactMismatch mismatch = (PactMismatch) result;
                throw new RuntimeException(mismatch.toString());
            }
        }
    }

    private String getProvider(TestClass testClass, PactVerification pactVerification) {
        if (!"".equals(pactVerification.value().trim())) {
            return pactVerification.value();
        } else {
            PactConsumerConfiguration pactConsumerConfiguration = pactConsumerConfigurationInstance.get();
            if (pactConsumerConfiguration.isProviderSet()) {
                return pactConsumerConfiguration.getProvider();
            } else {
                throw new IllegalArgumentException(
                        String.format("Provider name must be set either by using provider configuration property in arquillian.xml or annotating %s test with %s with a provider set.", testClass.getName(), PactVerification.class.getName()));
            }
        }
    }

    protected Optional<Method> findPactMethod(String currentProvider, TestClass testClass, PactVerification pactVerification) {
        String pactFragment = pactVerification.fragment();

        final Optional<Class<?>> classWithPactAnnotation = ResolveClassAnnotation.getClassWithAnnotation(testClass.getJavaClass(), Pact.class);
        final List<Method> pactMethods = findPactFragmentMethods(testClass);
        for (Method method : pactMethods) {
            Optional<Pact> pact = resolvePactAnnotation(classWithPactAnnotation, method);
            if (pact.isPresent() && pact.get().provider().equals(currentProvider)
                    && (pactFragment.isEmpty() || pactFragment.equals(method.getName()))) {

                validatePactSignature(method);
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }

    private Optional<Pact> resolvePactAnnotation(Optional<Class<?>> clazz, Method method) {
        Pact pactMethodAnnotation = method.getAnnotation(Pact.class);

        if (pactMethodAnnotation == null) {
            // It can be at class level.
            if (clazz.isPresent()) {
                return Optional.ofNullable(clazz.get().getAnnotation(Pact.class));
            } else {
                // method will be ignored.
                logger.log(Level.INFO, String.format("Method %s returns a %s type but it is not annotated at method nor at class level with %s",
                        method.getName(),
                        PactFragment.class.getName(),
                        Pact.class.getName()));
                return null;
            }
        } else {
            return Optional.of(pactMethodAnnotation);
        }

    }

    private void validatePactSignature(Method method) {
        boolean hasValidPactSignature =
                PactFragment.class.isAssignableFrom(method.getReturnType())
                        && method.getParameterTypes().length == 1
                        && method.getParameterTypes()[0].isAssignableFrom(PactDslWithProvider.class);

        if (!hasValidPactSignature) {
            throw new UnsupportedOperationException("Method " + method.getName() +
                    " does not conform required method signature 'public PactFragment xxx(PactDslWithProvider builder)'");
        }
    }

    private List<Method> findPactFragmentMethods(TestClass testClass) {
        final Method[] methods = testClass.getJavaClass().getMethods();

        return Arrays.stream(methods)
                .filter(method -> method.getReturnType().isAssignableFrom(PactFragment.class))
                .collect(Collectors.toList());
    }

}
