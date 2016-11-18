package org.arquillian.algeron.consumer.core;

import org.jboss.arquillian.core.spi.LoadableExtension;

public class AlgeronConsumerExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(AlgeronConsumerConfigurator.class)
                .observer(ContractsPublisher.class);
    }
}
