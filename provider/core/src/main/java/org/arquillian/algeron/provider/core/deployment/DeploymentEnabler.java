package org.arquillian.algeron.provider.core.deployment;

import org.arquillian.algeron.provider.core.AlgeronProviderConfiguration;
import org.jboss.arquillian.container.spi.event.DeployManagedDeployments;
import org.jboss.arquillian.container.spi.event.StartContainer;
import org.jboss.arquillian.container.spi.event.StopContainer;
import org.jboss.arquillian.container.spi.event.UnDeployManagedDeployments;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;

public class DeploymentEnabler {

    void skipDeployment(@Observes(precedence = -1000) EventContext<DeployManagedDeployments> eventContext, AlgeronProviderConfiguration algeronProviderConfiguration) {
        proceedIfEnabled(eventContext, algeronProviderConfiguration);
    }

    void skipUndeploy(@Observes(precedence = -1000) EventContext<UnDeployManagedDeployments> eventContext, AlgeronProviderConfiguration algeronProviderConfiguration) {
        proceedIfEnabled(eventContext, algeronProviderConfiguration);
    }

    void skipStartContainer(@Observes(precedence = -1000)EventContext<StartContainer> eventContext, AlgeronProviderConfiguration algeronProviderConfiguration) {
        proceedIfEnabled(eventContext, algeronProviderConfiguration);
    }

    void skipStopContainer(@Observes(precedence = -1000)EventContext<StopContainer> eventContext, AlgeronProviderConfiguration algeronProviderConfiguration) {
        proceedIfEnabled(eventContext, algeronProviderConfiguration);
    }

    private <T> void proceedIfEnabled(final EventContext<T> eventContext, AlgeronProviderConfiguration algeronProviderConfiguration) {
        if (shouldEnableDeployment(algeronProviderConfiguration)) {
            eventContext.proceed();
        }
    }

    public static boolean shouldEnableDeployment(AlgeronProviderConfiguration algeronProviderConfiguration) {
        return ! algeronProviderConfiguration.isSkipDeployment();
    }

}
