package org.arquillian.pact.consumer.spi.publisher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Encapsulates logic for storing pacts into a repository.
 * This interface is the public SPI to implement any pact storage.
 */
public interface PactPublisher {

    /**
     * Publish pacts to appropriate repository.
     * @param contractsSource where pact files are generated.
     */
    void publish(Path contractsSource) throws IOException;

    /**
     * Returns the name to identify this published in configuration file.
     * @return name of the published
     */
    String getName();

    /**
     * Method used for passing configuration parameters to publisher.
     * @param configuration key-values
     */
    void configure(Map<String, Object> configuration);

}
