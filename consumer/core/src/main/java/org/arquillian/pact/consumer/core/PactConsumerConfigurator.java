package org.arquillian.pact.consumer.core;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

import java.util.Map;

public class PactConsumerConfigurator {

    private static final String EXTENSION_NAME = "pact-consumer";

    @Inject
    @ApplicationScoped
    InstanceProducer<PactConsumerConfiguration> pactConsumerConfigurationInstanceProducer;

    public void configure(@Observes ArquillianDescriptor arquillianDescriptor) {
        Map<String, String> config = arquillianDescriptor.extension(EXTENSION_NAME).getExtensionProperties();
        PactConsumerConfiguration pactConsumerConfiguration = PactConsumerConfiguration.fromMap(config);

        this.pactConsumerConfigurationInstanceProducer.set(pactConsumerConfiguration);
    }


}
