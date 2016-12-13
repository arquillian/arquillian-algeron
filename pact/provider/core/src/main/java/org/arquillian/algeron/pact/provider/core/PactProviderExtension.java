package org.arquillian.algeron.pact.provider.core;

import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class PactProviderExtension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(PactsRetriever.class)
                .observer(InteractionRunner.class)
                .observer(PactProviderConfigurator.class)
                .observer(HttpTargetCreator.class)
                .service(ResourceProvider.class, HttpTargetResourceProvider.class);
    }
}
