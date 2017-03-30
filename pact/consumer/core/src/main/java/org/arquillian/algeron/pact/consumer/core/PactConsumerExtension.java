package org.arquillian.algeron.pact.consumer.core;

import org.arquillian.algeron.pact.consumer.core.client.MockProviderConfigCreator;
import org.arquillian.algeron.pact.consumer.core.client.ConsumerPactTest;
import org.arquillian.algeron.pact.consumer.core.client.PactConsumerArchiveAppender;
import org.arquillian.algeron.pact.consumer.core.client.PactDataReceiver;
import org.arquillian.algeron.pact.consumer.core.client.StandaloneConsumerPactTest;
import org.arquillian.algeron.pact.consumer.core.client.enricher.StubServerEnricher;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;

public class PactConsumerExtension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(PactConsumerConfigurator.class)
            .observer(MockProviderConfigCreator.class)
            .observer(PactReportDirectoryConfigurator.class)
            .service(TestEnricher.class, StubServerEnricher.class);

        if (Validate.classExists("org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender")) {
            builder.service(AuxiliaryArchiveAppender.class, PactConsumerArchiveAppender.class)
                .observer(ConsumerPactTest.class)
                .observer(PactDataReceiver.class);
        } else {
            // standalone
            builder.observer(StandaloneConsumerPactTest.class);
        }
    }
}
