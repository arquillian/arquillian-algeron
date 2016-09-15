package org.arquillian.pact.consumer.core.client;

import au.com.dius.pact.model.MockHttpsProviderConfig;
import au.com.dius.pact.model.MockProviderConfig;
import org.arquillian.pact.consumer.core.PactConsumerConfiguration;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

public class MockProviderConfigCreator {

    @Inject
    @ApplicationScoped
    InstanceProducer<MockProviderConfig> configInstanceProducer;

    public void create(@Observes BeforeClass beforeClass, PactConsumerConfiguration pactConsumerConfiguration) {

        if (pactConsumerConfiguration.isHttps()) {
            if (pactConsumerConfiguration.getPort() < 1) {
                final MockProviderConfig value = MockHttpsProviderConfig
                        .createDefault(pactConsumerConfiguration.getHost(),
                                pactConsumerConfiguration.getPactSpecVersion());
                configInstanceProducer.set(value);
            } else {
                final MockProviderConfig value = MockHttpsProviderConfig
                        .httpsConfig(pactConsumerConfiguration.getHost(),
                                pactConsumerConfiguration.getPort(),
                                pactConsumerConfiguration.getPactSpecVersion());
                configInstanceProducer.set(value);
            }
        } else {
            if (pactConsumerConfiguration.getPort() < 1) {
                final MockProviderConfig value = MockProviderConfig.createDefault(pactConsumerConfiguration.getHost(),
                        pactConsumerConfiguration.getPactSpecVersion());
                configInstanceProducer.set(value);
            } else {
                final MockProviderConfig value = MockProviderConfig.httpConfig(pactConsumerConfiguration.getHost(),
                        pactConsumerConfiguration.getPort(),
                        pactConsumerConfiguration.getPactSpecVersion());
                configInstanceProducer.set(value);
            }
        }

    }

}
