package org.arquillian.algeron.provider.core;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

import java.util.Map;

public class AlgeronProviderConfigurator {

    private static final String EXTENSION_NAME = "algeron-provider";

    @Inject
    @ApplicationScoped
    private InstanceProducer<AlgeronProviderConfiguration> configurationProducer;

    public void configure(@Observes(precedence = -10) ArquillianDescriptor arquillianDescriptor) {
        final Map<String, String> config = arquillianDescriptor.extension(EXTENSION_NAME).getExtensionProperties();

        final AlgeronProviderConfiguration algeronConsumerConfiguration = AlgeronProviderConfiguration.fromMap(config);
        configurationProducer.set(algeronConsumerConfiguration);

    }

}
