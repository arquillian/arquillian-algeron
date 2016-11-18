package org.arquillian.algeron.consumer.core;

import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class AlgeronConsumerConfiguration {

    private static final String PUBLISH_CONTRACTS = "publishContracts";
    private static final String PUBLISH_CONFIGURATION = "publishConfiguration";

    private boolean publishContracts = false;
    private Map<String, Object> publishConfiguration = null;

    public boolean isPublishContracts() {
        return publishContracts;
    }

    public boolean isPublishConfigurationSet() {
        return publishConfiguration != null;
    }

    public Map<String, Object> getPublishConfiguration() {
        return publishConfiguration;
    }

    public static final AlgeronConsumerConfiguration fromMap(Map<String, String> map) {
        AlgeronConsumerConfiguration algeronConfiguration = new AlgeronConsumerConfiguration();

        if (map.containsKey(PUBLISH_CONTRACTS)) {
            algeronConfiguration.publishContracts = Boolean.parseBoolean(map.get(PUBLISH_CONTRACTS));
        }

        if (map.containsKey(PUBLISH_CONFIGURATION)) {
            algeronConfiguration.publishConfiguration = loadConfiguration(map.get(PUBLISH_CONFIGURATION));
        }

        return algeronConfiguration;
    }

    private final static Map<String, Object> loadConfiguration(String configurationContent) {
        return (Map<String, Object>) new Yaml().load(configurationContent);
    }


}
