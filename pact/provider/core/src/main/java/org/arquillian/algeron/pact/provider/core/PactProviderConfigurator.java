package org.arquillian.algeron.pact.provider.core;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

import java.util.Map;

public class PactProviderConfigurator {

    private static final String EXTENSION_NAME = "pact-provider";

    @Inject
    @ApplicationScoped
    private InstanceProducer<PactProviderConfiguration> pactProviderConfigurationInstanceProducer;

    public void configure(@Observes ArquillianDescriptor arquillianDescriptor) {

        Map<String, String> config = arquillianDescriptor.extension(EXTENSION_NAME).getExtensionProperties();
        PactProviderConfiguration pactProviderConfiguration = PactProviderConfiguration.fromMap(config);

        pactProviderConfigurationInstanceProducer.set(pactProviderConfiguration);

    }

}
