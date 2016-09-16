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
import org.arquillian.pact.consumer.spi.Pact;
import org.arquillian.pact.consumer.spi.PactVerification;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.Test;

import java.lang.reflect.Method;
import java.util.Optional;


public abstract class AbstractConsumerPactTest {

    private static final VerificationResult PACT_VERIFIED = PactVerified$.MODULE$;

    @Inject
    protected Instance<PactConsumerConfiguration> pactConsumerConfigurationInstance;

    @Inject
    protected Instance<MockProviderConfig> mockProviderConfigInstance;

    protected void executeConsumerTest(EventContext<Test> testEventContext, TestClass testClass, PactVerification pactVerification) throws Throwable {
        String currentProvider = getProvider(testClass, pactVerification);

        // Start of execution

        final Object testInstance = testEventContext.getEvent().getTestInstance();
        PactFragment pactFragment = getPactFragment(testClass, currentProvider, testInstance, pactVerification);

        VerificationResult result = runPactTest(testEventContext, pactFragment);
        validateResult(result, pactVerification);
    }

    private PactFragment getPactFragment(TestClass testClass, String currentProvider, Object testInstance, PactVerification pactVerification) {
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
        return pactFragment;
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

    private Optional<Method> findPactMethod(String currentProvider, TestClass testClass, PactVerification pactVerification) {
        String pactFragment = pactVerification.fragment();

        final Method[] pactMethods = testClass.getMethods(Pact.class);
        for (Method method : pactMethods) {
            Pact pact = method.getAnnotation(Pact.class);
            if (pact != null && pact.provider().equals(currentProvider)
                    && (pactFragment.isEmpty() || pactFragment.equals(method.getName()))) {

                validatePactSignature(method);
                return Optional.of(method);
            }
        }
        return Optional.empty();
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

}
