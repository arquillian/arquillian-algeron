package org.arquillian.algeron.pact.provider.core;

import org.arquillian.algeron.pact.provider.core.httptarget.Target;
import org.arquillian.algeron.pact.provider.core.httptarget.HttpTarget;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

public class HttpTargetCreator {

    @Inject
    @ApplicationScoped
    InstanceProducer<Target> targetInstanceProducer;

    public void create(@Observes PactProviderConfiguration pactProviderConfiguration) {

        if (pactProviderConfiguration.isTargetUrlSet()) {
            targetInstanceProducer.set(new HttpTarget(pactProviderConfiguration.getTargetUrl(), pactProviderConfiguration.isInsecure()));
        } else {
            targetInstanceProducer.set(new HttpTarget(
                    pactProviderConfiguration.getProtocol(),
                    pactProviderConfiguration.getHost(),
                    pactProviderConfiguration.getPort(),
                    pactProviderConfiguration.getPath(),
                    pactProviderConfiguration.isInsecure()
                    )
            );
        }

    }

}
