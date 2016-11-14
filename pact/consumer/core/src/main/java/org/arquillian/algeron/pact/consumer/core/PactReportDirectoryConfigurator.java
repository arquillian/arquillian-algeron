package org.arquillian.algeron.pact.consumer.core;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStopping;

public class PactReportDirectoryConfigurator {

    public static final String PACT_ROOT_DIR = "pact.rootDir";

    private boolean customReportDirectory = false;

    public void configurePactReportDirectory(@Observes PactConsumerConfiguration pactConsumerConfiguration) {

        if (pactConsumerConfiguration.isPactReportDirSet()) {
            if (System.getProperty(PACT_ROOT_DIR) == null || System.getProperty(PACT_ROOT_DIR).trim().isEmpty()) {
                System.setProperty(PACT_ROOT_DIR, pactConsumerConfiguration.getPactReportDir());
                customReportDirectory = true;
            }
        }
    }

    public void unsetSystemProperty(@Observes ManagerStopping event) {
        if (customReportDirectory) {
            System.clearProperty(PACT_ROOT_DIR);
        }
    }

}
