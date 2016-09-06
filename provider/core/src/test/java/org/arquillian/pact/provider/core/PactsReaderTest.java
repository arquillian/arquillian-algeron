package org.arquillian.pact.provider.core;

import au.com.dius.pact.model.Consumer;
import au.com.dius.pact.model.Pact;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.loader.PactFolder;
import org.arquillian.pact.provider.api.Pacts;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PactsReaderTest {

    @Mock
    InstanceProducer instanceProducer;

    @Captor
    ArgumentCaptor<Pacts> argumentCaptor;

    @Test
    public void should_load_pacts_from_test() {

        final PactsReader pactsReader = new PactsReader();
        pactsReader.pactsInstanceProducer = instanceProducer;

        pactsReader.readPacts(new BeforeClass(PactDefinition.class));
        verify(instanceProducer).set(argumentCaptor.capture());

        Pacts pacts = argumentCaptor.getValue();
        final List<Pact> listOfLoadedPacts = pacts.getPacts();
        assertThat(listOfLoadedPacts).hasSize(1).element(0)
                .hasFieldOrPropertyWithValue("provider", new au.com.dius.pact.model.Provider("planets_provider"))
                .hasFieldOrPropertyWithValue("consumer", new Consumer("planets_consumer"));
    }

    @Provider("planets_provider")
    @PactFolder("pacts")
    public static class PactDefinition {

    }
}
