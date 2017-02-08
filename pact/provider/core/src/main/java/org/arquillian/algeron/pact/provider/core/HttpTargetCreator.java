package org.arquillian.algeron.pact.provider.core;

import org.arquillian.algeron.pact.provider.spi.Target;
import org.arquillian.algeron.pact.provider.core.httptarget.HttpTarget;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

public class HttpTargetCreator {

    @Inject
    @ApplicationScoped
    InstanceProducer<Target> targetInstanceProducer;

    @Inject
    Instance<Injector> injectorInstance;

    public void create(@Observes PactProviderConfiguration pactProviderConfiguration) {

        HttpTarget httpTarget;
        if (pactProviderConfiguration.isTargetUrlSet()) {
            httpTarget = new HttpTarget(pactProviderConfiguration.getTargetUrl(), pactProviderConfiguration.isInsecure());
        } else {
            httpTarget = new HttpTarget(
                    pactProviderConfiguration.getProtocol(),
                    pactProviderConfiguration.getHost(),
                    pactProviderConfiguration.getPort(),
                    pactProviderConfiguration.getPath(),
                    pactProviderConfiguration.isInsecure()
            );
        }

        httpTarget.setInjector(injectorInstance.get());
        targetInstanceProducer.set(httpTarget);
    }

}
