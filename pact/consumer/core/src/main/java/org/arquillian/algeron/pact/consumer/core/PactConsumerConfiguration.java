package org.arquillian.algeron.pact.consumer.core;

import au.com.dius.pact.model.PactSpecVersion;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;
import java.util.Properties;

public class PactConsumerConfiguration {

    private static final String BIND_MOCK_HOST = "host";
    private static final String MOCK_PORT = "port";
    private static final String PACT_VERSION = "pactSpecVersion";
    private static final String HTTPS = "https";
    private static final String PROVIDER = "provider";
    private static final String PACT_ARTIFACT_VERSION = "pactArtifactVersion";
    private static final String PACT_REPORT_DIR = "pactReportDir";
    private static final String PACT_PUBLISH_CONTRACTS = "publishContracts";
    private static final String PACT_PUBLISH_CONFIGURATION = "pactPublishConfiguration";


    private String host = "localhost";
    private int port = 9090;
    private PactSpecVersion pactSpecVersion = PactSpecVersion.V3;
    private boolean https = false;
    private String provider = null;
    private String pactArtifactVersion = null;
    private String pactReportDir = null;

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

    public boolean isPactReportDirSet() {
        return pactReportDir != null;
    }

    public String getPactReportDir() {
        return pactReportDir;
    }

    public boolean isPactArtifactVersionSet() {
        return pactArtifactVersion != null;
    }

    public String getPactArtifactVersion() {
        return pactArtifactVersion;
    }

    public boolean isProviderSet() {
        return this.provider != null;
    }

    public String getProvider() {
        return provider;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public PactSpecVersion getPactSpecVersion() {
        return pactSpecVersion;
    }

    public boolean isHttps() {
        return https;
    }

    public Properties asProperties() {
        Properties properties = new Properties();

        properties.put(BIND_MOCK_HOST, getHost());
        properties.put(MOCK_PORT, Integer.toString(getPort()));
        properties.put(PACT_VERSION, Integer.toString(pactSpecVersionAsInt(getPactSpecVersion())));
        properties.put(HTTPS, Boolean.toString(isHttps()));

        if (isPactReportDirSet()) {
            properties.put(PACT_REPORT_DIR, getPactReportDir());
        }

        if (isProviderSet()) {
            properties.put(PROVIDER, getProvider());
        }

        if (isPactArtifactVersionSet()) {
            properties.put(PACT_ARTIFACT_VERSION, getPactArtifactVersion());
        }

        return properties;
    }

    private int pactSpecVersionAsInt(PactSpecVersion pactSpecVersion) {
        switch(pactSpecVersion) {
            case V1:
            case V1_1:
                return 1;
            case V2: return 2;
            default: return 3;
        }
    }

    public static final PactConsumerConfiguration fromMap(Map<String, String> map) {
        PactConsumerConfiguration pactConsumerConfiguration = new PactConsumerConfiguration();

        if (map.containsKey(BIND_MOCK_HOST)) {
            pactConsumerConfiguration.host = map.get(BIND_MOCK_HOST);
        }

        if (map.containsKey(MOCK_PORT)) {
            pactConsumerConfiguration.port = Integer.parseInt(map.get(MOCK_PORT));
        }

        if (map.containsKey(PACT_VERSION)) {
            pactConsumerConfiguration.pactSpecVersion = PactSpecVersion.fromInt(Integer.parseInt(map.get(PACT_VERSION)));
        }

        if (map.containsKey(HTTPS)) {
            pactConsumerConfiguration.https = Boolean.parseBoolean(map.get(HTTPS));
        }

        if (map.containsKey(PROVIDER)) {
            pactConsumerConfiguration.provider = map.get(PROVIDER);
        }

        if (map.containsKey(PACT_ARTIFACT_VERSION)) {
            pactConsumerConfiguration.pactArtifactVersion = map.get(PACT_ARTIFACT_VERSION);
        }

        if (map.containsKey(PACT_REPORT_DIR)) {
            pactConsumerConfiguration.pactReportDir = map.get(PACT_REPORT_DIR);
        }

        if (map.containsKey(PACT_PUBLISH_CONTRACTS)) {
            pactConsumerConfiguration.publishContracts = Boolean.parseBoolean(map.get(PACT_PUBLISH_CONTRACTS));
        }

        if (map.containsKey(PACT_PUBLISH_CONFIGURATION)) {
            pactConsumerConfiguration.publishConfiguration = loadConfiguration(map.get(PACT_PUBLISH_CONFIGURATION));
        }

        return pactConsumerConfiguration;

    }

    private final static Map<String, Object> loadConfiguration(String configurationContent) {
        return (Map<String, Object>) new Yaml().load(configurationContent);
    }

}
