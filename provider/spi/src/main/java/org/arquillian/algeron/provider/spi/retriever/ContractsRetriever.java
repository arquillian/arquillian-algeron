package org.arquillian.algeron.provider.spi.retriever;


import java.io.IOException;
import java.net.URI;
import java.util.List;

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
     * @param providerName to set.
     */
    default void setProviderName(String providerName) {}

}