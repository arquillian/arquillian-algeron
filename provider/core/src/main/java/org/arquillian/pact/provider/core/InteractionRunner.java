package org.arquillian.pact.provider.core;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.event.suite.Test;

/**
 * Runner that will execute the same test for all defined Pacts
 */
public class InteractionRunner {

    public void executePacts(@Observes EventContext<Test> test) {
    }
}
