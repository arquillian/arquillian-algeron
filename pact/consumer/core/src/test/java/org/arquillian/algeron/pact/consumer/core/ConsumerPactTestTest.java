package org.arquillian.algeron.pact.consumer.core;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import org.arquillian.algeron.pact.consumer.core.client.StandaloneConsumerPactTest;
import org.arquillian.algeron.pact.consumer.spi.Pact;
import org.arquillian.algeron.pact.consumer.spi.PactVerification;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.test.spi.TestClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerPactTestTest {

    @Mock
    PactVerification pactVerification;

    @Mock
    PactConsumerConfiguration pactConsumerConfiguration;

    @Before
    public void setup() {
        when(pactVerification.fragment()).thenReturn("");
    }

    @Test
    public void should_get_pact_from_method() throws NoSuchMethodException {

        AbstractConsumerPactTest abstractConsumerPactTest = new StandaloneConsumerPactTest();
        TestClass testClass = new TestClass(PactMethod.class);
        final Optional<AbstractConsumerPactTest.PactMethod> pactFragmentMethod =
            abstractConsumerPactTest.findPactMethod("p1", testClass, pactVerification);

        final Method annotatedMethod = PactMethod.class.getMethod("contract1", PactDslWithProvider.class);
        assertThat(pactFragmentMethod.get().getMethod())
            .isEqualTo(annotatedMethod);
        assertThat(pactFragmentMethod.get().getPact())
            .isEqualTo(annotatedMethod.getAnnotation(Pact.class));
    }

    @Test
    public void should_get_pact_from_class() throws NoSuchMethodException {

        AbstractConsumerPactTest abstractConsumerPactTest = new StandaloneConsumerPactTest();
        TestClass testClass = new TestClass(PactClass.class);

        final Optional<AbstractConsumerPactTest.PactMethod> pactFragmentMethod =
            abstractConsumerPactTest.findPactMethod("p2", testClass, pactVerification);

        assertThat(pactFragmentMethod.get().getMethod())
            .isEqualTo(PactClass.class.getMethod("contract2", PactDslWithProvider.class));
        assertThat(pactFragmentMethod.get().getPact())
            .isEqualTo(PactClass.class.getAnnotation(Pact.class));
    }

    @Test
    public void should_give_preference_to_method_annotation() throws NoSuchMethodException {

        AbstractConsumerPactTest abstractConsumerPactTest = new StandaloneConsumerPactTest();
        TestClass testClass = new TestClass(PactMethodClass.class);

        final Optional<AbstractConsumerPactTest.PactMethod> pactFragmentMethod =
            abstractConsumerPactTest.findPactMethod("p4", testClass, pactVerification);

        final Method annotatedMethod = PactMethodClass.class.getMethod("contract3", PactDslWithProvider.class);
        assertThat(pactFragmentMethod.get().getMethod())
            .isEqualTo(annotatedMethod);
        assertThat(pactFragmentMethod.get().getPact())
            .isEqualTo(annotatedMethod.getAnnotation(Pact.class));
    }

    @Test
    public void should_ignore_class_annotation_if_annotated_method() throws NoSuchMethodException {

        AbstractConsumerPactTest abstractConsumerPactTest = new StandaloneConsumerPactTest();
        TestClass testClass = new TestClass(PactMethodClass.class);
        final Optional<AbstractConsumerPactTest.PactMethod> pactFragmentMethod =
            abstractConsumerPactTest.findPactMethod("p3", testClass, pactVerification);

        Assertions.assertThat(pactFragmentMethod).isNotPresent();
    }

    @Test
    public void should_get_provider_name_from_pact_class_annotation() {
        AbstractConsumerPactTest abstractConsumerPactTest = new StandaloneConsumerPactTest();
        TestClass testClass = new TestClass(PactClassClassProvider.class);

        when(pactVerification.value()).thenReturn("");

        final String provider = abstractConsumerPactTest.getProvider(testClass,
            pactVerification);
        assertThat(provider).isEqualTo("p2");
    }

    @Test
    public void should_get_provider_name_from_pact_provider_method_annotation() {
        AbstractConsumerPactTest abstractConsumerPactTest = new StandaloneConsumerPactTest();
        TestClass testClass = new TestClass(PactClassMethodProvider.class);

        when(pactVerification.value()).thenReturn("p3");

        final String provider = abstractConsumerPactTest.getProvider(testClass,
            pactVerification);
        assertThat(provider).isEqualTo("p3");
    }

    @Test
    public void should_get_provider_name_from_configuration() {
        AbstractConsumerPactTest abstractConsumerPactTest = new StandaloneConsumerPactTest();
        TestClass testClass = new TestClass(PactMethodPactVerificationWithoutProvider.class);

        when(pactVerification.value()).thenReturn("");
        when(pactConsumerConfiguration.getProvider()).thenReturn("p4");
        when(pactConsumerConfiguration.isProviderSet()).thenReturn(true);
        abstractConsumerPactTest.pactConsumerConfigurationInstance = () -> pactConsumerConfiguration;

        final String provider = abstractConsumerPactTest.getProvider(testClass,
            pactVerification);
        assertThat(provider).isEqualTo("p4");
    }

    public static class PactMethod {

        @Pact(consumer = "c1", provider = "p1")
        public RequestResponsePact contract1(PactDslWithProvider builder) {
            return null;
        }
    }

    @Pact(consumer = "c2", provider = "p2")
    public static class PactClass {
        public RequestResponsePact contract2(PactDslWithProvider builder) {
            return null;
        }
    }

    @Pact(consumer = "c3", provider = "p3")
    public static class PactMethodClass {
        @Pact(consumer = "c4", provider = "p4")
        public RequestResponsePact contract3(PactDslWithProvider builder) {
            return null;
        }
    }

    @Pact(consumer = "c2", provider = "p2")
    public static class PactClassClassProvider {
        public RequestResponsePact contract2(PactDslWithProvider builder) {
            return null;
        }
    }

    @Pact(consumer = "c2", provider = "p2")
    public static class PactClassMethodProvider {
        public RequestResponsePact contract2(PactDslWithProvider builder) {
            return null;
        }
    }

    public static class PactMethodPactVerificationWithoutProvider {

        @Pact(consumer = "c1", provider = "p1")
        public RequestResponsePact contract1(PactDslWithProvider builder) {
            return null;
        }
    }
}
