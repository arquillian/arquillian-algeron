package org.arquillian.algeron.pact.consumer.publisher.pactbroker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class PactBrokerContractsPublisherTest {

    @Test
    public void should_publish_directory_of_contracts() throws IOException {
        final Map<String, Object> configuration = createConfiguration();

        PactBorkerContractsPublisher pactBorkerContractsPublisher = new PactBorkerContractsPublisher();
        pactBorkerContractsPublisher.configure(configuration);
        pactBorkerContractsPublisher.publish();

    }

    private Map<String, Object> createConfiguration() {
        final Map<String, Object> configuration = new HashMap<>();
        configuration.put(PactBorkerContractsPublisher.URL, "https://redhat.pact.dius.com.au");
        configuration.put(PactBorkerContractsPublisher.USERNAME, "");
        configuration.put(PactBorkerContractsPublisher.PASSWORD, "");
        configuration.put(PactBorkerContractsPublisher.CONTRACTS_FOLDER, "src/test/resources/pacts");
        configuration.put(PactBorkerContractsPublisher.VERSION, "1.0.1");
        return configuration;
    }
}
