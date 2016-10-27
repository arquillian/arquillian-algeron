package org.arquillian.pact.provider.core.deployment;


import org.arquillian.pact.provider.api.deployment.Environment;
import org.arquillian.pact.provider.core.PactProviderConfiguration;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class EnvironmentUrlResourceProvuderTest {

    @Test
    public void should_resolve_url_if_environment_annotation_present() throws NoSuchFieldException {

        try {
            System.setProperty("urlvar", "http://www.arquillian.org");
            EnvironmentUrlResourceProvider environmentUrlResourceProvider = new EnvironmentUrlResourceProvider();
            environmentUrlResourceProvider.pactProviderConfigurationInstance = () -> {
                final HashMap<String, String> config = new HashMap<>();
                config.put("skipDeployment", "true");
                return PactProviderConfiguration.fromMap(config);
            };

            final Field urlField = TestWithFieldEnricher.class.getField("url");
            final Object url = environmentUrlResourceProvider.doLookup(urlField.getAnnotation(ArquillianResource.class), urlField.getAnnotation(Environment.class));
            assertThat(url).isInstanceOf(URL.class);
            URL urlObject = (URL) url;
            assertThat(urlObject).hasHost("www.arquillian.org");
        } finally {
            System.clearProperty("urlvar");
        }
    }


    static class TestWithFieldEnricher {
        @Environment("urlvar")
        @ArquillianResource
        public URL url;

        public URL getUrl() {
            return url;
        }
    }

}
