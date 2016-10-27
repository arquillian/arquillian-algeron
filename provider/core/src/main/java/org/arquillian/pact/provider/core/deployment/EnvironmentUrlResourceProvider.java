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

public class EnvironmentUrlResourceProvider extends URLResourceProvider {

    private static final int NOT_FOUND = -1;

    @Inject
    Instance<PactProviderConfiguration> pactProviderConfigurationInstance;

    @Override
    public Object doLookup(ArquillianResource resource, Annotation... qualifiers) {
        if (DeploymentEnabler.shouldEnableDeployment(pactProviderConfigurationInstance.get())) {
            return super.doLookup(resource, qualifiers);
        } else {
            int position = NOT_FOUND;
            if ((position = getEnvironmentAnnotationPosition(qualifiers)) == NOT_FOUND) {
                return super.doLookup(resource, qualifiers);
            } else {
                return resolveAnnotation((Environment) qualifiers[position]);
            }
        }
    }

    private int getEnvironmentAnnotationPosition(Annotation[] annotations) {

        for (int i = 0; i < annotations.length; i++) {
            if (Environment.class.equals(annotations[i].annotationType())) {
                return i;
            }
        }

        return NOT_FOUND;
    }

    private URL resolveAnnotation(Environment environmentVar) {
        String resolvedUrl = Optional.ofNullable(System.getenv(environmentVar.value()))
                .orElse(Optional.ofNullable(System.getProperty(environmentVar.value()))
                        .orElse(""));
        URL url;
        try {
            url = new URL(resolvedUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        return url;
    }

}
