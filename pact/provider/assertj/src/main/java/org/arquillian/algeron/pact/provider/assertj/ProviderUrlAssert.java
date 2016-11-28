package org.arquillian.algeron.pact.provider.assertj;

import java.net.URL;

public class ProviderUrlAssert {

    private PactProviderAssertions pactProviderAssertions;
    private URL url;

    ProviderUrlAssert(PactProviderAssertions pactProviderAssertions, URL url) {
        this.pactProviderAssertions = pactProviderAssertions;
        this.url = url;
    }

    public void verifiesContract() {
        this.pactProviderAssertions.target.testInteraction(url);
    }

}
