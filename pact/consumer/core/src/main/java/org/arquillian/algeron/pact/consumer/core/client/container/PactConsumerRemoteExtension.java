package org.arquillian.algeron.pact.consumer.core.client.container;

import org.arquillian.algeron.pact.consumer.core.client.MockProviderConfigCreator;
import org.arquillian.algeron.pact.consumer.core.client.enricher.StubServerEnricher;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;

public class PactConsumerRemoteExtension implements RemoteLoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(MockProviderConfigCreator.class)
                .observer(RemoteConsumerPactTest.class)
                .observer(PactConsumerConfigurator.class)
                .service(TestEnricher.class, StubServerEnricher.class);

    }
}
