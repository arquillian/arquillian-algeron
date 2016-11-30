package org.arquillian.algeron.pact.provider.core;

import au.com.dius.pact.model.Consumer;
import au.com.dius.pact.model.Pact;
import au.com.dius.pact.model.ProviderState;
import au.com.dius.pact.model.RequestResponseInteraction;
import org.arquillian.algeron.pact.provider.core.httptarget.Target;
import org.arquillian.algeron.pact.provider.api.Pacts;
import org.arquillian.algeron.pact.provider.spi.CurrentConsumer;
import org.arquillian.algeron.pact.provider.spi.CurrentInteraction;
import org.arquillian.algeron.pact.provider.spi.Provider;
import org.arquillian.algeron.pact.provider.spi.State;
import org.arquillian.algeron.provider.core.retriever.ContractsFolder;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InteractionRunnerTest {

    @Mock
    private EventContext<Test> eventContext;

    @Mock
    private Test test;

    @Mock
    private Target target;

    @Mock
    private RequestResponseInteraction requestResponseInteraction;

    @Mock
    private ProviderState providerState;

    private Instance<Pacts> pactsInstance;

    @Before
    public void setup() {
        final PactsReader pactsReader = new PactsReader();
        final List<Pact> pacts = pactsReader.getPacts(new BeforeClass(PactProvider.class));
        pactsInstance = () -> new Pacts(pacts);

        when(eventContext.getEvent()).thenReturn(test);

    }

    @org.junit.Test
    public void should_execute_test_for_each_interaction() {
        when(test.getTestClass()).thenReturn(new TestClass(PactProvider.class));
        PactProvider pactDefinition = new PactProvider();
        when(test.getTestInstance()).thenReturn(pactDefinition);

        InteractionRunner interactionRunner = new InteractionRunner();
        interactionRunner.pactsInstance = pactsInstance;
        interactionRunner.targetInstance = () -> target;
        interactionRunner.executePacts(eventContext);

        assertThat(pactDefinition.consumer).isEqualTo(new Consumer("planets_consumer"));
        assertThat(pactDefinition.interaction).isNotNull();

        verify(eventContext, times(2)).proceed();

    }

    @org.junit.Test
    public void should_throw_exception_when_no_target() {
        when(test.getTestClass()).thenReturn(new TestClass(PactProviderWithNoTarget.class));
        PactProviderWithNoTarget pactDefinition = new PactProviderWithNoTarget();
        when(test.getTestInstance()).thenReturn(pactDefinition);

        InteractionRunner interactionRunner = new InteractionRunner();
        interactionRunner.pactsInstance = pactsInstance;
        interactionRunner.targetInstance = () -> target;

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> interactionRunner.executePacts(eventContext))
                .withMessage("Field annotated with org.jboss.arquillian.test.api.ArquillianResource should implement org.arquillian.algeron.pact.provider.core.httptarget.Target and didn't found any");

    }

    @org.junit.Test
    public void should_execute_states_with_regular_expression_syntax_for_simple_types() {

        when(providerState.getName()).thenReturn("I have 36 cukes in my belly");
        final List<ProviderState> providerStates = new ArrayList<>();
        providerStates.add(providerState);
        when(requestResponseInteraction.getProviderStates()).thenReturn(providerStates);

        InteractionRunner interactionRunner = new InteractionRunner();

        TestClass testClass = new TestClass(PactProviderWithIntegerParameterStateMethod.class);
        PactProviderWithIntegerParameterStateMethod test = new PactProviderWithIntegerParameterStateMethod();
        interactionRunner.executeStateChanges(requestResponseInteraction, testClass, test);

        assertThat(test.getNumberOfCukes())
                .isEqualTo(36);

    }

    @org.junit.Test
    public void should_execute_states_with_regular_expression_syntax_for_collection_types() {

        when(providerState.getName()).thenReturn("The following animals: cow, pig, bug");
        final List<ProviderState> providerStates = new ArrayList<>();
        providerStates.add(providerState);
        when(requestResponseInteraction.getProviderStates()).thenReturn(providerStates);

        InteractionRunner interactionRunner = new InteractionRunner();

        TestClass testClass = new TestClass(PactProviderWithListParameterStateMethod.class);
        PactProviderWithListParameterStateMethod test = new PactProviderWithListParameterStateMethod();
        interactionRunner.executeStateChanges(requestResponseInteraction, testClass, test);

        assertThat(test.getAnimals())
                .contains("cow", "pig", "bug");

    }

    @Provider("planets_provider")
    @ContractsFolder("pacts")
    public static class PactProviderWithNoTarget {

        @CurrentConsumer
        Consumer consumer;

        @CurrentInteraction
        RequestResponseInteraction interaction;

    }

    @Provider("planets_provider")
    @ContractsFolder("pacts")
    public static class PactProvider {

        @CurrentConsumer
        Consumer consumer;

        @CurrentInteraction
        RequestResponseInteraction interaction;

        @ArquillianResource
        Target target;

    }

    @Provider("planets_provider")
    @ContractsFolder("pacts")
    public static class PactProviderWithIntegerParameterStateMethod {

        private int numberOfCukes = 0;

        @State("I have (\\d+) cukes in my belly")
        public void stateMethod(int numberOfCukes) {
            this.numberOfCukes = numberOfCukes;
        }

        @CurrentConsumer
        Consumer consumer;

        @CurrentInteraction
        RequestResponseInteraction interaction;

        @ArquillianResource
        Target target;

        public int getNumberOfCukes() {
            return numberOfCukes;
        }
    }

    @Provider("planets_provider")
    @ContractsFolder("pacts")
    public static class PactProviderWithListParameterStateMethod {

        private List<String> animals = new ArrayList<>();

        @State("The following animals: (.*)")
        public void stateMethod(List<String> animals) {
            this.animals = animals;
        }

        @CurrentConsumer
        Consumer consumer;

        @CurrentInteraction
        RequestResponseInteraction interaction;

        @ArquillianResource
        Target target;

        public List<String> getAnimals() {
            return animals;
        }
    }

}
