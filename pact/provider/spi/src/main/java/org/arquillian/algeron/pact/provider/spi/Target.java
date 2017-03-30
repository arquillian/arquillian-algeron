package org.arquillian.algeron.pact.provider.spi;

import au.com.dius.pact.model.RequestResponseInteraction;

import java.net.URL;

/**
 * Run {@link au.com.dius.pact.model.RequestResponseInteraction} and perform response verification
 */
public interface Target {

    /**
     * Run {@link au.com.dius.pact.model.RequestResponseInteraction} and perform response verification
     * <p>
     * Any exception will be caught by caller and reported as test failure
     * <p>
     * Important: Implementators must implements {@link PactProviderExecutionAwareTarget}
     * <p>
     * URL configuration is retrieved from PactProviderConfiguration
     */
    void testInteraction();

    /**
     * Run {@link au.com.dius.pact.model.RequestResponseInteraction} and perform response verification
     * <p>
     * Any exception will be caught by caller and reported as test failure
     * <p>
     * Important: Implementators must implements {@link PactProviderExecutionAwareTarget}
     *
     * @param url
     *     where provider is deployed
     */
    void testInteraction(URL url);

    /**
     * Run {@link au.com.dius.pact.model.RequestResponseInteraction} and perform response verification
     * <p>
     * Any exception will be caught by caller and reported as test failure
     *
     * @param url
     *     where provider is deployed
     * @param consumer
     *     consumer name that generated the interaction
     * @param interaction
     *     interaction to be tested
     */
    void testInteraction(URL url, String consumer, RequestResponseInteraction interaction);

    /**
     * Run {@link au.com.dius.pact.model.RequestResponseInteraction} and perform response verification
     * <p>
     * Any exception will be caught by caller and reported as test failure
     *
     * @param consumer
     *     consumer name that generated the interaction
     * @param interaction
     *     interaction to be tested
     */
    void testInteraction(String consumer, RequestResponseInteraction interaction);
}
