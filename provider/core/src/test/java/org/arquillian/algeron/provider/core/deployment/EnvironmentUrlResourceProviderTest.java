package org.arquillian.algeron.provider.core.deployment;


import org.arquillian.algeron.provider.api.deployment.Environment;
import org.arquillian.algeron.provider.core.AlgeronProviderConfiguration;
import org.jboss.arquillian.container.test.impl.enricher.resource.URLResourceProvider;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnvironmentUrlResourceProviderTest {

    @Mock
    URLResourceProvider urlResourceProvider;

    @Mock
    Injector injector;

    @Test
    public void should_resolve_url_if_environment_annotation_present() throws NoSuchFieldException {

        try {
            System.setProperty("urlvar", "http://www.arquillian.org");
            EnvironmentUrlResourceProvider environmentUrlResourceProvider = new EnvironmentUrlResourceProvider();
            environmentUrlResourceProvider.algeronProviderConfigurationInstance = () -> {
                final HashMap<String, String> config = new HashMap<>();
                config.put("skipDeployment", "true");
                return AlgeronProviderConfiguration.fromMap(config);
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

    @Test
    public void should_resolve_url_with_default_provider() throws NoSuchFieldException, MalformedURLException {

        try {
            System.setProperty("urlvar", "http://www.arquillian.org");
            EnvironmentUrlResourceProvider environmentUrlResourceProvider = new EnvironmentUrlResourceProvider();
            environmentUrlResourceProvider.algeronProviderConfigurationInstance = () -> {
                final HashMap<String, String> config = new HashMap<>();
                config.put("skipDeployment", "false");
                return AlgeronProviderConfiguration.fromMap(config);
            };

            when(injector.inject(Matchers.any())).thenReturn(urlResourceProvider);
            when(urlResourceProvider.doLookup(Matchers.any(), Matchers.any())).thenReturn(new URL("http://mocked.org"));

            environmentUrlResourceProvider.injectorInstance = () -> injector;

            final Field urlField = TestWithFieldEnricher.class.getField("url");
            final Object url = environmentUrlResourceProvider.doLookup(urlField.getAnnotation(ArquillianResource.class), urlField.getAnnotation(Environment.class));

            assertThat(url).isInstanceOf(URL.class);
            URL urlObject = (URL) url;
            assertThat(urlObject).hasHost("mocked.org");
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
