package org.arquillian.pact.provider.core;

import au.com.dius.pact.model.Consumer;
import au.com.dius.pact.model.Pact;
import au.com.dius.pact.model.RequestResponseInteraction;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.loader.PactFolder;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import org.arquillian.pact.provider.api.Pacts;
import org.arquillian.pact.provider.spi.CurrentConsumer;
import org.arquillian.pact.provider.spi.CurrentInteraction;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InteractionRunnerTest {

    @Mock
    private EventContext<Test> eventContext;

    @Mock
    private Test test;

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
        interactionRunner.executePacts(eventContext);

        assertThat(pactDefinition.consumer).isEqualTo(new Consumer("planets_consumer"));
        assertThat(pactDefinition.interaction).isNotNull();

        verify(eventContext, times(2)).proceed();

    }

    @org.junit.Test
    public void should_throw_exception_when_no_resources() {
        when(test.getTestClass()).thenReturn(new TestClass(PactProviderWithNoArqResources.class));
        PactProviderWithNoArqResources pactDefinition = new PactProviderWithNoArqResources();
        when(test.getTestInstance()).thenReturn(pactDefinition);

        InteractionRunner interactionRunner = new InteractionRunner();
        interactionRunner.pactsInstance = pactsInstance;

        try {
            interactionRunner.executePacts(eventContext);
            fail("Exception should be thrown");
        } catch(IllegalArgumentException e) {
            assertThat(e).hasMessage("Only one field annotated with org.arquillian.pact.provider.spi.CurrentInteraction of type au.com.dius.pact.model.RequestResponseInteraction should be present * Only one field annotated with org.arquillian.pact.provider.spi.CurrentConsumer of type au.com.dius.pact.model.Consumer should be present");
        }

    }

    @org.junit.Test
    public void should_throw_exception_when_no_target() {
        when(test.getTestClass()).thenReturn(new TestClass(PactProviderWithNoTarget.class));
        PactProviderWithNoTarget pactDefinition = new PactProviderWithNoTarget();
        when(test.getTestInstance()).thenReturn(pactDefinition);

        InteractionRunner interactionRunner = new InteractionRunner();
        interactionRunner.pactsInstance = pactsInstance;

        try {
            interactionRunner.executePacts(eventContext);
            fail("Exception should be thrown");
        } catch(IllegalArgumentException e) {
            assertThat(e).hasMessage("Test class should have exactly one field annotated with au.com.dius.pact.provider.junit.target.TestTarget");
        }

    }

    @Provider("planets_provider")
    @PactFolder("pacts")
    public static class PactProviderWithNoTarget {

        @CurrentConsumer
        Consumer consumer;

        @CurrentInteraction
        RequestResponseInteraction interaction;

    }

    @Provider("planets_provider")
    @PactFolder("pacts")
    public static class PactProviderWithNoArqResources {

        @TestTarget
        Target target;

    }

    @Provider("planets_provider")
    @PactFolder("pacts")
    public static class PactProvider {

        @CurrentConsumer
        Consumer consumer;

        @CurrentInteraction
        RequestResponseInteraction interaction;

        @TestTarget
        Target target;

    }
}
