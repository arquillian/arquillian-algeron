package org.arquillian.pact.provider.core.deployment;


import org.arquillian.pact.provider.api.deployment.Environment;
import org.arquillian.pact.provider.core.PactProviderConfiguration;
import org.jboss.arquillian.container.test.impl.enricher.resource.URLResourceProvider;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;

import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

/**
 * URL Resource Provider that in case of annotating a URL with ArquillianResource and
 * {@link Environment} the value of the URL is got from provided system property or environment variable
 * instead of using the common Arquillian enrichment.
 */
public class EnvironmentUrlResourceProvider extends URLResourceProvider {

    @Inject
    Instance<PactProviderConfiguration> pactProviderConfigurationInstance;

    @Override
    public Object doLookup(ArquillianResource resource, Annotation... qualifiers) {
        if (DeploymentEnabler.shouldEnableDeployment(pactProviderConfigurationInstance.get())) {
            return super.doLookup(resource, qualifiers);
        } else {
            final Optional<Environment> environmentAnnotation = getEnvironmentAnnotation(qualifiers);
            if (environmentAnnotation.isPresent()) {
                return resolveAnnotation(environmentAnnotation.get());
            } else {
                return super.doLookup(resource, qualifiers);
            }
        }
    }

    private Optional<Environment> getEnvironmentAnnotation(Annotation[] annotations) {

        for (int i = 0; i < annotations.length; i++) {
            if (Environment.class.equals(annotations[i].annotationType())) {
                return Optional.of((Environment) annotations[i]);
            }
        }

        return Optional.empty();
    }

    private URL resolveAnnotation(Environment environmentVar) {
        String resolvedUrl = Optional.ofNullable(System.getenv(environmentVar.value()))
                .orElse(Optional.ofNullable(System.getProperty(environmentVar.value()))
                        .orElse(""));
        try {
            return new URL(resolvedUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
