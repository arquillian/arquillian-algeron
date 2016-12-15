package org.arquillian.algeron.consumer.core;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;

import java.io.IOException;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logic that takes configured pact publisher and publish generated pact files.
 */
public class ContractsPublisherObserver {

    private static Logger logger = Logger.getLogger(ContractsPublisherObserver.class.getName());
    private static final String PROVIDER = "provider";

    public void publish(@Observes AfterClass event, AlgeronConsumerConfiguration algeronConsumerConfiguration) throws IOException {
        if (algeronConsumerConfiguration.isPublishContracts() && algeronConsumerConfiguration.isPublishConfigurationSet()) {

            final Map<String, Object> publishConfiguration = algeronConsumerConfiguration.getPublishConfiguration();
            if (publishConfiguration.containsKey(PROVIDER)) {
                final String providerName = (String) publishConfiguration.get(PROVIDER);

                final org.arquillian.algeron.consumer.spi.publisher.ContractsPublisher contractsPublisher = getContractPublisher(providerName);
                contractsPublisher.configure(publishConfiguration);
                contractsPublisher.publish();
            } else {
                logger.log(Level.WARNING, String.format("Publishing contracts are enabled, but configuration is not providing a %s property with provider name to be used.", PROVIDER));
            }
        }
    }

    private org.arquillian.algeron.consumer.spi.publisher.ContractsPublisher getContractPublisher(String name) {
        ServiceLoader<org.arquillian.algeron.consumer.spi.publisher.ContractsPublisher> pactPublisherServiceLoader = ServiceLoader.load(org.arquillian.algeron.consumer.spi.publisher.ContractsPublisher.class);

        for (org.arquillian.algeron.consumer.spi.publisher.ContractsPublisher contractsPublisher : pactPublisherServiceLoader) {
            if (contractsPublisher.getName().equals(name)) {
                return contractsPublisher;
            }
        }

        throw new IllegalArgumentException(String.format("No contract publisher registered with name %s.", name));
    }

}
