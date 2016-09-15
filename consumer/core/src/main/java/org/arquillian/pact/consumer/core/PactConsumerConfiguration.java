package org.arquillian.pact.consumer.core;

import au.com.dius.pact.model.PactSpecVersion;

import java.util.Map;

public class PactConsumerConfiguration {

    private static final String BIND_MOCK_HOST = "host";
    private static final String MOCK_PORT = "port";
    private static final String PACT_VERSION = "pactSpecVersion";
    private static final String HTTPS = "https";
    private static final String PROVIDER = "provider";


    private String host = "localhost";
    private int port = 9090;
    private PactSpecVersion pactSpecVersion = PactSpecVersion.V2;
    private boolean https = false;
    private String provider = null;

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

        return pactConsumerConfiguration;

    }

}
