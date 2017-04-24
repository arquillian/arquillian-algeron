package org.arquillian.algeron.pact.consumer.publisher.pactbroker;

import au.com.dius.pact.provider.broker.PactBrokerClient;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.arquillian.algeron.consumer.spi.publisher.ContractsPublisher;

public class PactBorkerContractsPublisher implements ContractsPublisher {

    private static final Logger logger = Logger.getLogger(PactBorkerContractsPublisher.class.getName());

    static final String URL = "url";
    static final String CONTRACTS_FOLDER = "contractsFolder";
    static final String USERNAME = "username";
    static final String PASSWORD = "password";
    static final String VERSION = "version";

    private Map<String, Object> configuration;

    @Override
    public void publish() throws IOException {

        final String contractFolder = (String) this.configuration.get(CONTRACTS_FOLDER);
        final String version = this.configuration.containsKey(VERSION) ? (String) this.configuration.get(VERSION) : "latest";

        final String url = containsAuthenticationOptions() ? getAuthenticateUrl((String) this.configuration.get(URL)) : (String) this.configuration.get(URL);
        final PactBrokerClient pactBrokerClient = new PactBrokerClient(url);

        publishContractFiles(pactBrokerClient, Paths.get(contractFolder), version);

    }

    private boolean containsAuthenticationOptions() {
        return this.configuration.containsKey(USERNAME) && this.configuration.containsKey(PASSWORD);
    }

    private void publishContractFiles(PactBrokerClient pactBrokerClient, Path contractsLocation, String version) throws IOException {
        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.json");
        try (Stream<Path> stream = Files.walk(contractsLocation)) {
            stream
                .filter(matcher::matches)
                .peek(path -> logger.log(Level.INFO, String.format("Publishing to Pact Broker contract %s", path)))
                .forEach(path -> {
                    final String result = (String) pactBrokerClient.uploadPactFile(path.toFile(), version);

                    if (result.startsWith("FAILED!")) {
                        throw new IllegalStateException(String.format("Failed to publish %s contract with server failure %s", path, result));
                    }

                });
        }

    }

    private String getAuthenticateUrl(String url) {
        final String username = (String) this.configuration.get(USERNAME);
        final String password = (String) this.configuration.get(PASSWORD);

        final int slashIndex = url.indexOf("//");

        if (slashIndex > -1) {
            url = String.format("%s%s:%s@%s", url.substring(0, slashIndex + 2), username, password, url.substring(slashIndex + 2));
        } else {
            url = String.format("%s:%s@%s", username, password, url);
        }
        return url;
    }

    @Override
    public String getName() {
        return "pactbroker";
    }

    @Override
    public void configure(Map<String, Object> configuration) {

        this.configuration = configuration;

        if (!this.configuration.containsKey(URL)) {
            throw new IllegalArgumentException(String.format("To use Pact Broker Publisher you need to set %s of the broker", URL));
        }

        if (!(this.configuration.get(URL) instanceof String)) {
            throw new IllegalArgumentException(String.format("Pact Broker Publisher requires %s configuration property to be an String instead of %s", URL, this.configuration.get(URL)));
        }

        if (!this.configuration.containsKey(CONTRACTS_FOLDER)) {
            throw new IllegalArgumentException(String.format("Pact Borker Publisher requires %s configuration property", CONTRACTS_FOLDER));
        }

        if (!(this.configuration.get(CONTRACTS_FOLDER) instanceof String)) {
            throw new IllegalArgumentException(String.format("Pact Broker Publisher requires %s configuration property to be an String", CONTRACTS_FOLDER));
        }

    }
}
