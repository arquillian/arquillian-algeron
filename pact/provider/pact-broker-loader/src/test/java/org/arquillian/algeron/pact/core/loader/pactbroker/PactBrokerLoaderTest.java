package org.arquillian.algeron.pact.core.loader.pactbroker;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.arquillian.algeron.pact.provider.core.loader.pactbroker.PactBrokerLoader;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
public class PactBrokerLoaderTest {

    @Test
    public void should_read_pact_broker_contracts() throws IOException {

        final Map<String, Object> configuration = createConfiguration();
        PactBrokerLoader pactBrokerLoader = new PactBrokerLoader();
        pactBrokerLoader.configure(createConfiguration());
        pactBrokerLoader.setProviderName("planets_provider");

        final List<URI> retrieveContracts = pactBrokerLoader.retrieve();

        assertThat(retrieveContracts).hasSize(1);
    }

    private Map<String, Object> createConfiguration() {
        final Map<String, Object> configuration = new HashMap<>();
        configuration.put("url", "https://redhat.pact.dius.com.au");
        configuration.put("username", "");
        configuration.put("password", "");
        return configuration;
    }

}
