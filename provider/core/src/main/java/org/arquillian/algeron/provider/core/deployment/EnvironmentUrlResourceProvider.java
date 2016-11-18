package org.arquillian.algeron.provider.core.deployment;


import org.arquillian.algeron.configuration.Reflection;
import org.arquillian.algeron.provider.api.deployment.Environment;
import org.arquillian.algeron.provider.core.AlgeronProviderConfiguration;
import org.jboss.arquillian.container.test.impl.enricher.resource.OperatesOnDeploymentAwareProvider;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.api.ArquillianResource;

import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

/**
 * URL Resource Provider that in case of annotating a URL with ArquillianResource and
 * {@link org.arquillian.algeron.provider.api.deployment.Environment} the value of the URL is got from provided system property or environment variable
 * instead of using the common Arquillian enrichment.
 */
public class EnvironmentUrlResourceProvider extends OperatesOnDeploymentAwareProvider {

    private static final String CUSTOM_SWARM_URL_PROVIDER = "org.wildfly.swarm.arquillian.resources.SwarmURIResourceProvider";

    private static final String DEFAULT_URL_PROVIDER = "org.jboss.arquillian.container.test.impl.enricher.resource.URLResourceProvider";

    @Inject
    Instance<AlgeronProviderConfiguration> algeronProviderConfigurationInstance;

    @Inject
    Instance<Injector> injectorInstance;

    public EnvironmentUrlResourceProvider() {
        super();
    }

    @Override
    public Object doLookup(ArquillianResource resource, Annotation... qualifiers) {
        if (DeploymentEnabler.shouldEnableDeployment(algeronProviderConfigurationInstance.get())) {
            return resolveResourceProvider(resource, qualifiers);
        } else {
            final Optional<Environment> environmentAnnotation = getEnvironmentAnnotation(qualifiers);
            if (environmentAnnotation.isPresent()) {
                return resolveAnnotation(environmentAnnotation.get());
            } else {
                return resolveResourceProvider(resource, qualifiers);
            }
        }
    }

    private Object resolveResourceProvider(ArquillianResource resource, Annotation[] qualifiers) {
        if (isSwarmUrlProviderOverriden()) {
            return createSwarmUrlResourceProvider().doLookup(resource, qualifiers);
        } else {
            return createDefaultUrlResourceProvider().doLookup(resource, qualifiers);
        }
    }

    private OperatesOnDeploymentAwareProvider createDefaultUrlResourceProvider() {
        try {
            Class<? extends OperatesOnDeploymentAwareProvider> defaultUrlProvider = (Class<? extends OperatesOnDeploymentAwareProvider>) Class.forName(DEFAULT_URL_PROVIDER);
            OperatesOnDeploymentAwareProvider urlResourceProvider = Reflection.newInstance(defaultUrlProvider, new Class[0], new Object[0]);

            return injectorInstance.get().inject(urlResourceProvider);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private OperatesOnDeploymentAwareProvider createSwarmUrlResourceProvider() {
        try {
            Class<? extends OperatesOnDeploymentAwareProvider> swarmUrlProvider = (Class<? extends OperatesOnDeploymentAwareProvider>) Class.forName(CUSTOM_SWARM_URL_PROVIDER);
            OperatesOnDeploymentAwareProvider urlResourceProvider = Reflection.newInstance(swarmUrlProvider, new Class[0], new Object[0]);

            return injectorInstance.get().inject(urlResourceProvider);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private boolean isSwarmUrlProviderOverriden() {
        return LoadableExtension.Validate.classExists(CUSTOM_SWARM_URL_PROVIDER);
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

    @Override
    public boolean canProvide(Class<?> type) {
        return type.isAssignableFrom(URL.class);
    }
}
