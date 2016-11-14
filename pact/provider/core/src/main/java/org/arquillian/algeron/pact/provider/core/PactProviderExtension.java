package org.arquillian.algeron.pact.provider.core;

import org.arquillian.algeron.pact.provider.core.deployment.DeploymentEnabler;
import org.arquillian.algeron.pact.provider.core.deployment.EnvironmentUrlResourceProvider;
import org.jboss.arquillian.container.test.impl.enricher.resource.URLResourceProvider;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class PactProviderExtension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(PactsReader.class)
                .observer(InteractionRunner.class)
                .observer(PactProviderConfigurator.class)
                .observer(HttpTargetCreator.class)
                .service(ResourceProvider.class, HttpTargetResourceProvider.class);

        if(Validate.classExists("org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender")) {
            builder.observer(DeploymentEnabler.class);
            builder.override(ResourceProvider.class, URLResourceProvider.class, EnvironmentUrlResourceProvider.class);
        }
    }
}
