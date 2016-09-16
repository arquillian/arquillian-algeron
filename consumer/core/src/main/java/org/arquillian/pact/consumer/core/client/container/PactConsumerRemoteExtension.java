package org.arquillian.pact.consumer.core.client.container;

import org.arquillian.pact.consumer.core.client.MockProviderConfigCreator;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;

public class PactConsumerRemoteExtension implements RemoteLoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(MockProviderConfigCreator.class)
                .observer(RemoteConsumerPactTest.class)
                .observer(PactConsumerConfigurator.class);

    }
}
