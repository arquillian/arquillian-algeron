package org.arquillian.algeron.pact.consumer.core;

import org.arquillian.algeron.pact.consumer.spi.publisher.PactPublisher;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logic that takes configured pact publisher and publish generated pact files.
 */
public class PactsPublisher {

    private static Logger logger = Logger.getLogger(PactsPublisher.class.getName());
    private static final String PROVIDER = "provider";

    public void publish(@Observes AfterClass event, PactConsumerConfiguration pactConsumerConfiguration) throws IOException {
        if (pactConsumerConfiguration.isPublishContracts() && pactConsumerConfiguration.isPublishConfigurationSet()) {

            final Map<String, Object> publishConfiguration = pactConsumerConfiguration.getPublishConfiguration();
            if (publishConfiguration.containsKey(PROVIDER)) {
                final String providerName = (String) publishConfiguration.get(PROVIDER);

                final PactPublisher pactPublisher = getPactPublisher(providerName);
                pactPublisher.configure(publishConfiguration);
                pactPublisher.publish(getPactFilesDirectory());
            } else {
                logger.log(Level.WARNING, String.format("Publishing contracts are enabled, but configuration is not providing a %s property with provider name to be used.", PROVIDER));
            }
        }
    }

    private Path getPactFilesDirectory() {
        String directory = System.getProperty(PactReportDirectoryConfigurator.PACT_ROOT_DIR, "target/pacts");
        return Paths.get(directory);
    }

    private PactPublisher getPactPublisher(String name) {
        ServiceLoader<PactPublisher> pactPublisherServiceLoader = ServiceLoader.load(PactPublisher.class);

        for (PactPublisher pactPublisher : pactPublisherServiceLoader) {
            if (pactPublisher.getName().equals(name)) {
                return pactPublisher;
            }
        }

        throw new IllegalArgumentException(String.format("No pact publisher registered with name %s.", name));
    }

}
