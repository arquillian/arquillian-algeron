package org.arquillian.pact.consumer.core;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import org.arquillian.pact.consumer.core.client.StandaloneConsumerPactTest;
import org.arquillian.pact.consumer.spi.Pact;
import org.arquillian.pact.consumer.spi.PactVerification;
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

    @Before
    public void setup() {
        when(pactVerification.fragment()).thenReturn("");
    }

    @Test
    public void should_get_pact_from_method() throws NoSuchMethodException {

        AbstractConsumerPactTest abstractConsumerPactTest = new StandaloneConsumerPactTest();
        TestClass testClass = new TestClass(PactMethod.class);
        final Optional<Method> pactFragmentMethod = abstractConsumerPactTest.findPactMethod("p1", testClass, pactVerification);

        assertThat(pactFragmentMethod).isPresent().contains(PactMethod.class.getMethod("contract1", PactDslWithProvider.class));

    }

    @Test
    public void should_get_pact_from_class() throws NoSuchMethodException {

        AbstractConsumerPactTest abstractConsumerPactTest = new StandaloneConsumerPactTest();
        TestClass testClass = new TestClass(PactClass.class);
        final Optional<Method> pactFragmentMethod = abstractConsumerPactTest.findPactMethod("p2", testClass, pactVerification);

        assertThat(pactFragmentMethod).isPresent().contains(PactClass.class.getMethod("contract2", PactDslWithProvider.class));

    }

    @Test
    public void should_give_preference_to_method_annotation() throws NoSuchMethodException {

        AbstractConsumerPactTest abstractConsumerPactTest = new StandaloneConsumerPactTest();
        TestClass testClass = new TestClass(PactMethodClass.class);
        final Optional<Method> pactFragmentMethod = abstractConsumerPactTest.findPactMethod("p4", testClass, pactVerification);

        assertThat(pactFragmentMethod).isPresent().contains(PactMethodClass.class.getMethod("contract3", PactDslWithProvider.class));

    }

    @Test
    public void should_ignore_class_annotation_if_annotated_method() throws NoSuchMethodException {

        AbstractConsumerPactTest abstractConsumerPactTest = new StandaloneConsumerPactTest();
        TestClass testClass = new TestClass(PactMethodClass.class);
        final Optional<Method> pactFragmentMethod = abstractConsumerPactTest.findPactMethod("p3", testClass, pactVerification);

        assertThat(pactFragmentMethod).isNotPresent();

    }

    public static class PactMethod {

        @Pact(consumer = "c1", provider = "p1")
        public PactFragment contract1(PactDslWithProvider builder) {
            return null;
        }

    }

    @Pact(consumer = "c2", provider = "p2")
    public static class PactClass {
        public PactFragment contract2(PactDslWithProvider builder) {
            return null;
        }
    }

    @Pact(consumer = "c3", provider = "p3")
    public static class PactMethodClass {
        @Pact(consumer = "c4", provider = "p4")
        public PactFragment contract3(PactDslWithProvider builder) {
            return null;
        }
    }

}
