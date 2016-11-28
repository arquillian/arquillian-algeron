package org.arquillian.algeron.pact.provider.assertj;

import org.arquillian.algeron.pact.provider.core.httptarget.Target;
import org.assertj.core.api.Assertions;

import java.net.URL;


public class PactProviderAssertions extends Assertions {

    Target target;

    PactProviderAssertions(Target target) {
        this.target = target;
    }

    public static PactProviderAssertions assertThat(Target target) {
        return new PactProviderAssertions(target);
    }

    public void verifiesContract() {
        this.target.testInteraction();
    }

    public ProviderUrlAssert withUrl(URL url) {
        return new ProviderUrlAssert(this, url);
    }

}
