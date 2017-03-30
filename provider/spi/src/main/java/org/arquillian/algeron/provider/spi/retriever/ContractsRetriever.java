package org.arquillian.algeron.provider.spi.retriever;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Encapsulate logic for getting contracts
 */
public interface ContractsRetriever {
    /**
     * Load contracts from appropriate source
     *
     * @return list of location of contracts.
     */
    List<URI> retrieve() throws IOException;

    /**
     * Method called to provide the provider name to retreiver. Most of retrievers don't need it.
     *
     * @param providerName
     *     to set.
     */
    default void setProviderName(String providerName) {
    }

    /**
     * Method used for passing configuration parameters to publisher.
     * This method is intended to be called when configuring retriver in arquillian.xml
     *
     * @param configuration
     *     key-values
     */
    void configure(Map<String, Object> configuration);

    /**
     * Returns the name to identify this published in configuration file.
     *
     * @return name of the published
     */
    String getName();
}