package org.arquillian.algeron.pact.provider.core.loader.pactbroker;

import au.com.dius.pact.provider.ConsumerInfo;
import au.com.dius.pact.provider.broker.PactBrokerClient;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import org.arquillian.algeron.provider.spi.retriever.ContractsRetriever;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Out-of-the-box implementation of {@link org.arquillian.algeron.provider.spi.retriever.ContractsRetriever} that
 * downloads pacts from Pact broker
 */
public class PactBrokerLoader implements ContractsRetriever {
    private String providerName;
    private PactBroker pactBroker;

    public PactBrokerLoader() {
    }

    public PactBrokerLoader(PactBroker pactBroker) {
        this.pactBroker = pactBroker;
    }

    @Override
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    @Override
    public void configure(Map<String, Object> configuration) {
        this.pactBroker = new ExternallyConfiguredContractsPactBroker(configuration);
    }

    @Override
    public String getName() {
        return "pactbroker";
    }

    @Override
    public List<URI> retrieve() throws IOException {

        final String url =
            containsAuthenticationOptions() ? getAuthenticateUrl(this.pactBroker.url()) : this.pactBroker.url();
        final PactBrokerClient pactBrokerClient = new PactBrokerClient(url);

        if (this.pactBroker.tags().length == 0) {

            final List<ConsumerInfo> consumerInfos = pactBrokerClient.fetchConsumers(this.providerName);
            return toUri(consumerInfos);
        } else {
            final List<URI> contracts = new ArrayList<>();
            for (String tag : this.pactBroker.tags()) {
                contracts.addAll(
                    toUri(pactBrokerClient.fetchConsumersWithTag(this.providerName, tag)));
            }

            return contracts;
        }
    }

    private List<URI> toUri(List<ConsumerInfo> consumerInfos) {
        return consumerInfos.stream()
            .map(ConsumerInfo::getPactFile)
            .map(u -> {
                try {
                    return ((java.net.URL) u).toURI();
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(e);
                }
            })
            .collect(Collectors.toList());
    }

    private boolean containsAuthenticationOptions() {
        return !"".equals(this.pactBroker.userame().trim()) && !"".equals(this.pactBroker.password().trim());
    }

    private String getAuthenticateUrl(String url) {
        final String username = this.pactBroker.userame();
        final String password = this.pactBroker.password();

        final int slashIndex = url.indexOf("//");

        if (slashIndex > -1) {
            url = String.format("%s%s:%s@%s", url.substring(0, slashIndex + 2), username, password,
                url.substring(slashIndex + 2));
        } else {
            url = String.format("%s:%s@%s", username, password, url);
        }
        return url;
    }

    static class ExternallyConfiguredContractsPactBroker implements PactBroker {

        static final String URL = "url";
        static final String USERNAME = "username";
        static final String PASSWORD = "password";
        static final String TAGS = "tags";

        String url;
        String username = "";
        String password = "";
        String[] tags = new String[0];

        public ExternallyConfiguredContractsPactBroker(Map<String, Object> configuration) {

            if (!configuration.containsKey(URL)) {
                throw new IllegalArgumentException(
                    String.format("To use Pact Broker Publisher you need to set %s of the broker", URL));
            }

            if (!(configuration.get(URL) instanceof String)) {
                throw new IllegalArgumentException(String.format(
                    "Pact Broker Publisher requires %s configuration property to be an String instead of %s", URL,
                    configuration.get(URL)));
            }

            url = (String) configuration.get(URL);

            if (configuration.containsKey(USERNAME)) {
                username = (String) configuration.get(USERNAME);
            }

            if (configuration.containsKey(PASSWORD)) {
                password = (String) configuration.get(PASSWORD);
            }
            if (configuration.containsKey(TAGS)) {
                Object tags = configuration.get(TAGS);

                if (tags instanceof String) {
                    String tagsString = (String) tags;
                    this.tags = tagsString.split(",");
                }

                if (tags instanceof Collection) {
                    Collection<String> tagsCollection = (Collection<String>) tags;
                    this.tags = tagsCollection.toArray(new String[tagsCollection.size()]);
                }
            }
        }

        @Override
        public String url() {
            return url;
        }

        @Override
        public String userame() {
            return username;
        }

        @Override
        public String password() {
            return password;
        }

        @Override
        public String[] tags() {
            return tags;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return PactBroker.class;
        }
    }
}
