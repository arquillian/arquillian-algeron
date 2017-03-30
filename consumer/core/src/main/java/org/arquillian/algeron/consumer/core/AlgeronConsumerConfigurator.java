package org.arquillian.algeron.consumer.core;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

import java.util.Map;

public class AlgeronConsumerConfigurator {

    private static final String EXTENSION_NAME = "algeron-consumer";

    @Inject
    @ApplicationScoped
    private InstanceProducer<AlgeronConsumerConfiguration> configurationProducer;

    public void configure(@Observes(precedence = -10) ArquillianDescriptor arquillianDescriptor) {
        final Map<String, String> config = arquillianDescriptor.extension(EXTENSION_NAME).getExtensionProperties();

        final AlgeronConsumerConfiguration algeronConsumerConfiguration = AlgeronConsumerConfiguration.fromMap(config);
        configurationProducer.set(algeronConsumerConfiguration);
    }
}
