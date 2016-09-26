package org.arquillian.pact.provider.core.httptarget;

import au.com.dius.pact.model.RequestResponseInteraction;

import java.net.URL;

/**
 * Run {@link au.com.dius.pact.model.RequestResponseInteraction} and perform response verification
 *
 * @see HttpTarget out-of-the-box implementation
 */
public interface Target {

    /**
     *
     * Run {@link au.com.dius.pact.model.RequestResponseInteraction} and perform response verification
     * <p>
     * Any exception will be caught by caller and reported as test failure
     *
     * Important: Implementators must implements {@link org.arquillian.pact.provider.spi.PactProviderExecutionAwareTarget}
     *
     * @param url where provider is deployed
     */
    void testInteraction(URL url);

    /**
     * Run {@link au.com.dius.pact.model.RequestResponseInteraction} and perform response verification
     * <p>
     * Any exception will be caught by caller and reported as test failure
     *
     * @param url where provider is deployed
     * @param consumer consumer name that generated the interaction
     * @param interaction interaction to be tested
     */
    void testInteraction(URL url, String consumer, RequestResponseInteraction interaction);

    /**
     * Run {@link au.com.dius.pact.model.RequestResponseInteraction} and perform response verification
     * <p>
     * Any exception will be caught by caller and reported as test failure
     *
     * @param consumer consumer name that generated the interaction
     * @param interaction interaction to be tested
     */
    void testInteraction(String consumer, RequestResponseInteraction interaction);

}
