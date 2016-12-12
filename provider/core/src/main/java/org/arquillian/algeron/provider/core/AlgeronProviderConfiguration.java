package org.arquillian.algeron.provider.core;

import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class AlgeronProviderConfiguration {

    private static final String SKIP_DEPLOYMENT = "skipDeployment";
    private static final String RETRIEVER_CONFIGURATION = "retrieverConfiguration";

    private boolean skipDeployment = false;
    private Map<String, Object> retrieverConfiguration = null;

    public boolean isSkipDeployment() {
        return skipDeployment;
    }

    public boolean isRetrieverConfigurationSet() {
        return this.retrieverConfiguration != null;
    }

    public Map<String, Object> getRetrieverConfiguration() {
        return retrieverConfiguration;
    }

    public static AlgeronProviderConfiguration fromMap(Map<String, String> config) {
        AlgeronProviderConfiguration configuration = new AlgeronProviderConfiguration();

        if (config.containsKey(SKIP_DEPLOYMENT)) {
            configuration.skipDeployment = Boolean.parseBoolean(config.get(SKIP_DEPLOYMENT));
        }

        if (config.containsKey(RETRIEVER_CONFIGURATION)) {
            configuration.retrieverConfiguration = loadConfiguration(config.get(RETRIEVER_CONFIGURATION));
        }

        return configuration;
    }

    private final static Map<String, Object> loadConfiguration(String configurationContent) {
        return (Map<String, Object>) new Yaml().load(configurationContent);
    }
}
