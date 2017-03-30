package org.arquillian.algeron.pact.provider.assertj;

import org.arquillian.algeron.pact.provider.spi.Target;
import org.assertj.core.api.Assertions;

import java.net.MalformedURLException;
import java.net.URL;

public class PactProviderAssertions extends Assertions {

    private Target target;
    private URL url;

    PactProviderAssertions(Target target) {
        this.target = target;
    }

    public static PactProviderAssertions assertThat(Target target) {
        return new PactProviderAssertions(target);
    }

    public void satisfiesContract() {
        if (this.url == null) {
            this.target.testInteraction();
        } else {
            this.target.testInteraction(this.url);
        }
    }

    public PactProviderAssertions withUrl(URL url) {
        this.url = url;
        return this;
    }

    public PactProviderAssertions withUrl(String url) {
        try {
            return withUrl(new URL(url));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
