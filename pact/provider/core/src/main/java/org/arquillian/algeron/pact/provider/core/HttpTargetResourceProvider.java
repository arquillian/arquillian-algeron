package org.arquillian.algeron.pact.provider.core;

import org.arquillian.algeron.pact.provider.spi.Target;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

import java.lang.annotation.Annotation;

public class HttpTargetResourceProvider implements ResourceProvider {

    @Inject
    private Instance<Target> instance;

    @Override
    public boolean canProvide(Class<?> type) {
        return Target.class.isAssignableFrom(type);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        Target target = this.instance.get();

        if(target == null) {
            throw new IllegalStateException("Target instance was not found");
        }

        return target;
    }
}
