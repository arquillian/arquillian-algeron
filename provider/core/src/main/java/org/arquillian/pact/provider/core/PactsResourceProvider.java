package org.arquillian.pact.provider.core;

import au.com.dius.pact.model.Pact;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

import java.lang.annotation.Annotation;

/**
 * Class that enrich a test with the list of current pact.
 */
public class PactsResourceProvider implements ResourceProvider {

    @Inject
    Instance<Pact> pactInstance;

    @Override
    public boolean canProvide(Class<?> type) {
        return type.isAssignableFrom(Pact.class);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        return pactInstance.get();
    }
}
