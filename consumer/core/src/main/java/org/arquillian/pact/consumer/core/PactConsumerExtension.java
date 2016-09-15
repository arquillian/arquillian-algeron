package org.arquillian.pact.consumer.core;

import org.arquillian.pact.consumer.core.client.ConsumerPactTest;
import org.arquillian.pact.consumer.core.client.MockProviderConfigCreator;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class PactConsumerExtension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(PactConsumerConfigurator.class)
                .observer(MockProviderConfigCreator.class)
                .observer(ConsumerPactTest.class);
    }
}
