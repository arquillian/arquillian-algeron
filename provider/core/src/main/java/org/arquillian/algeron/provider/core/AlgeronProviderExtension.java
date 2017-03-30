package org.arquillian.algeron.provider.core;

import org.arquillian.algeron.provider.core.deployment.DeploymentEnabler;
import org.arquillian.algeron.provider.core.deployment.EnvironmentUrlResourceProvider;
import org.jboss.arquillian.container.test.impl.enricher.resource.URLResourceProvider;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class AlgeronProviderExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {

        builder.observer(AlgeronProviderConfigurator.class);

        if (LoadableExtension.Validate.classExists("org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender")) {
            builder.observer(DeploymentEnabler.class);
            builder.override(ResourceProvider.class, URLResourceProvider.class, EnvironmentUrlResourceProvider.class);
        }

    }
}
