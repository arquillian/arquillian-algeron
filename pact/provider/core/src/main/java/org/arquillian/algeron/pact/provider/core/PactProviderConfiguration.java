package org.arquillian.algeron.pact.provider.core;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class PactProviderConfiguration {

    private static final String HTTP_TARGET_URL = "targetUrl";
    private static final String INSECURE = "insecure";
    private static final String PROTOCOL = "protocol";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String PATH = "path";
    private static final String SKIP_DEPLOYMENT = "skipDeployment";

    private URL targetUrl;
    private boolean insecure = false;

    private String protocol = "http";
    private String host = "localhost";
    private int port = 8080;
    private String path = "/";

    private boolean skipDeployment = false;

    private PactProviderConfiguration() {
        super();
    }


    public URL getTargetUrl() {
        return targetUrl;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getPath() {
        return path;
    }

    public boolean isInsecure() {
        return insecure;
    }

    public boolean isSkipDeployment() {
        return skipDeployment;
    }

    public boolean isTargetUrlSet() {
        return this.getTargetUrl() != null;
    }

    public static PactProviderConfiguration fromMap(Map<String, String> config) {
        PactProviderConfiguration configuration = new PactProviderConfiguration();

        if (config.containsKey(HTTP_TARGET_URL)) {
            try {
                configuration.targetUrl = new URL(config.get(HTTP_TARGET_URL));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }

        if (config.containsKey(INSECURE)) {
            configuration.insecure = Boolean.parseBoolean(config.get(INSECURE));
        }

        if (config.containsKey(PROTOCOL)) {
            configuration.protocol = config.get(PROTOCOL);
        }

        if (config.containsKey(HOST)) {
            configuration.host = config.get(HOST);
        }

        if (config.containsKey(PORT)) {
            configuration.port = Integer.parseInt(config.get(PORT));
        }

        if (config.containsKey(PATH)) {
            configuration.path = config.get(PATH);
        }

        if (config.containsKey(SKIP_DEPLOYMENT)) {
            configuration.skipDeployment = Boolean.parseBoolean(config.get(SKIP_DEPLOYMENT));
        }

        return configuration;
    }
}
