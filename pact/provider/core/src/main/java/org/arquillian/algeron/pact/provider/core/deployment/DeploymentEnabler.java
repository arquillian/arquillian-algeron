package org.arquillian.algeron.pact.provider.core.deployment;

import org.arquillian.algeron.pact.provider.core.PactProviderConfiguration;
import org.jboss.arquillian.container.spi.event.DeployManagedDeployments;
import org.jboss.arquillian.container.spi.event.StartContainer;
import org.jboss.arquillian.container.spi.event.StopContainer;
import org.jboss.arquillian.container.spi.event.UnDeployManagedDeployments;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;

public class DeploymentEnabler {

    void skipDeployment(@Observes(precedence = -1000) EventContext<DeployManagedDeployments> eventContext, PactProviderConfiguration pactProviderConfiguration) {
        proceedIfEnabled(eventContext, pactProviderConfiguration);
    }

    void skipUndeploy(@Observes(precedence = -1000) EventContext<UnDeployManagedDeployments> eventContext, PactProviderConfiguration pactProviderConfiguration) {
        proceedIfEnabled(eventContext, pactProviderConfiguration);
    }

    void skipStartContainer(@Observes(precedence = -1000)EventContext<StartContainer> eventContext, PactProviderConfiguration pactProviderConfiguration) {
        proceedIfEnabled(eventContext, pactProviderConfiguration);
    }

    void skipStopContainer(@Observes(precedence = -1000)EventContext<StopContainer> eventContext, PactProviderConfiguration pactProviderConfiguration) {
        proceedIfEnabled(eventContext, pactProviderConfiguration);
    }

    private <T> void proceedIfEnabled(final EventContext<T> eventContext, PactProviderConfiguration pactProviderConfiguration) {
        if (shouldEnableDeployment(pactProviderConfiguration)) {
            eventContext.proceed();
        }
    }

    public static boolean shouldEnableDeployment(PactProviderConfiguration pactProviderConfiguration) {
        return ! pactProviderConfiguration.isSkipDeployment();
    }

}
