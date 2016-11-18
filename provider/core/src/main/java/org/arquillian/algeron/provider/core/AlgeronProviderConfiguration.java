package org.arquillian.algeron.provider.core;

import java.util.Map;

public class AlgeronProviderConfiguration {

    private static final String SKIP_DEPLOYMENT = "skipDeployment";

    private boolean skipDeployment = false;

    public boolean isSkipDeployment() {
        return skipDeployment;
    }

    public static AlgeronProviderConfiguration fromMap(Map<String, String> config) {
        AlgeronProviderConfiguration configuration = new AlgeronProviderConfiguration();

        if (config.containsKey(SKIP_DEPLOYMENT)) {
            configuration.skipDeployment = Boolean.parseBoolean(config.get(SKIP_DEPLOYMENT));
        }

        return configuration;
    }
}
