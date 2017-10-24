package org.arquillian.algeron.pact.provider.core;

import au.com.dius.pact.model.Consumer;
import au.com.dius.pact.model.Pact;
import org.arquillian.algeron.pact.provider.api.Pacts;
import org.arquillian.algeron.pact.provider.spi.Provider;
import org.arquillian.algeron.provider.core.AlgeronProviderConfiguration;
import org.arquillian.algeron.provider.core.retriever.ContractsFolder;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PactsRetrieverTest {

    @Mock
    InstanceProducer instanceProducer;

    @Mock
    Instance instance;

    @Captor
    ArgumentCaptor<Pacts> argumentCaptor;

    @Test
    public void should_load_pacts_from_annotation_test() {

        // Given
        final PactsRetriever pactsRetriever = new PactsRetriever();
        pactsRetriever.pactsInstanceProducer = instanceProducer;
        pactsRetriever.algeronProviderConfigurationInstance = instance;

        // When
        pactsRetriever.retrievePacts(new BeforeClass(PactDefinition.class));

        // Then
        verify(instanceProducer).set(argumentCaptor.capture());
        Pacts pacts = argumentCaptor.getValue();
        final List<Pact> listOfLoadedPacts = pacts.getPacts();
        assertThat(listOfLoadedPacts).hasSize(1).element(0)
            .hasFieldOrPropertyWithValue("provider", new au.com.dius.pact.model.Provider("planets_provider"))
            .hasFieldOrPropertyWithValue("consumer", new Consumer("planets_consumer"));
    }

    @Test
    public void should_load_pacts_from_algeron_provider_configuration() {

        // Given
        final PactsRetriever pactsRetriever = new PactsRetriever();
        pactsRetriever.pactsInstanceProducer = instanceProducer;
        pactsRetriever.algeronProviderConfigurationInstance = instance;

        String retriever = "provider: folder" + System.lineSeparator() + "contractsFolder: pacts";
        final Map<String, String> configuration = new HashMap<>();
        configuration.put("retrieverConfiguration", retriever);

        final AlgeronProviderConfiguration algeronProviderConfiguration =
            AlgeronProviderConfiguration.fromMap(configuration);

        when(instance.get()).thenReturn(algeronProviderConfiguration);

        // When
        pactsRetriever.retrievePacts(new BeforeClass(NonePactDefinition.class));

        // Then
        verify(instanceProducer).set(argumentCaptor.capture());
        Pacts pacts = argumentCaptor.getValue();
        final List<Pact> listOfLoadedPacts = pacts.getPacts();
        assertThat(listOfLoadedPacts).hasSize(1).element(0)
            .hasFieldOrPropertyWithValue("provider", new au.com.dius.pact.model.Provider("planets_provider"))
            .hasFieldOrPropertyWithValue("consumer", new Consumer("planets_consumer"));
    }

    @Provider("planets_provider")
    @ContractsFolder("pacts")
    public static class PactDefinition {

    }

    @Provider("planets_provider")
    public static class NonePactDefinition {
    }
}
