package org.arquillian.algeron.pact.provider.assertj;

import org.arquillian.algeron.pact.provider.spi.Target;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.net.URL;

import static org.arquillian.algeron.pact.provider.assertj.PactProviderAssertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PactProviderAssertionsTest {

    @Mock
    Target target;


    @Test
    public void should_test_interactions_with_url_assertj() throws MalformedURLException {

        final URL url = new URL("http://www.google.com");
        assertThat(target).withUrl(url).satisfiesContract();
        verify(target).testInteraction(url);

    }

    @Test
    public void should_test_interactions_without_url_assertj() throws MalformedURLException {

        assertThat(target).satisfiesContract();
        verify(target).testInteraction();

    }

}
