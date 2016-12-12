package org.arquillian.algeron.pact.provider.core.loader.pactbroker;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.arquillian.algeron.configuration.RunnerExpressionParser;
import org.arquillian.algeron.provider.spi.retriever.ContractsRetriever;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * Out-of-the-box implementation of {@link org.arquillian.algeron.provider.spi.retriever.ContractsRetriever} that downloads pacts from Pact broker
 */
public class PactBrokerLoader implements ContractsRetriever {
    private static final Logger LOGGER = Logger.getLogger(PactBrokerLoader.class.getName());
    private static final String PACT_URL_PATTERN = "/pacts/provider/{0}/latest";
    private static final String PACT_URL_PATTERN_WITH_TAG = "/pacts/provider/{0}/latest/{1}";

    private String providerName;
    private PactBroker pactBroker;

    private final Retryer<HttpResponse> retryer = RetryerBuilder.<HttpResponse>newBuilder()
            .retryIfResult(response -> response.getStatusLine().getStatusCode() >= 500)
            .withWaitStrategy(WaitStrategies.exponentialWait(100, 1, TimeUnit.SECONDS))
            .withStopStrategy(StopStrategies.stopAfterDelay(5000))
            .build();
    private Callable<HttpResponse> httpResponseCallable;

    public PactBrokerLoader() {
        super();
    }


    @Override
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    @Override
    public void configure(Map<String, Object> configuration) {
        this.pactBroker = new PactBrokerImpl(configuration);
    }

    @Override
    public String getName() {
        return "pactbroker";
    }

    @Override
    public List<URI> retrieve() throws IOException {
        List<URI> pacts = new ArrayList<>();
        for (String tag: Arrays.stream(pactBroker.tags())
                .map(tag -> getResolvedValue(tag))
                .collect(toList())) {
            pacts.addAll(loadPactsForProvider(providerName, tag));
        }
        return pacts;
    }

    private List<URI> loadPactsForProvider(final String providerName, final String tag) throws IOException {
        LOGGER.log(Level.FINER, String.format("Loading pacts from pact broker for provider %s and tag %s ", providerName, tag));
        final HttpResponse httpResponse;
        try {
            URIBuilder uriBuilder = new URIBuilder().setScheme(RunnerExpressionParser.parseExpressions(pactBroker.protocol()))
                    .setHost(RunnerExpressionParser.parseExpressions(pactBroker.host()))
                    .setPort(Integer.parseInt(RunnerExpressionParser.parseExpressions(pactBroker.port())));
            if (tag.equals("latest")) {
                uriBuilder.setPath(MessageFormat.format(PACT_URL_PATTERN, providerName));
            } else {
                uriBuilder.setPath(MessageFormat.format(PACT_URL_PATTERN_WITH_TAG, providerName, tag));
            }
            URI brokerUri = uriBuilder.build();
            if (httpResponseCallable == null) {
                httpResponse = retryer.call(() -> Request.Get(brokerUri)
                        .setHeader(HttpHeaders.ACCEPT, "application/hal+json")
                        .execute().returnResponse());
            } else {
                httpResponse = retryer.call(httpResponseCallable);
            }
        } catch (final ExecutionException | RetryException | URISyntaxException e) {
            throw new IOException("Was not able load pacts from broker", e);
        }

        final int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == 404) {
            LOGGER.log(Level.WARNING, String.format("There are no pacts found for the service %s and tag %s", providerName, tag));
            return Collections.emptyList();
        }

        final InputStream content = httpResponse.getEntity().getContent();
        if (statusCode / 100 != 2) {
            throw new RuntimeException("Pact broker responded with status: " + statusCode +
                    "\n payload: '" + asStringPreservingNewLines(content) + "'");
        }

        final JsonValue parse = Json.parse(new InputStreamReader(content));
        final JsonObject fullList = parse.asObject();
        final JsonObject links = fullList.get("_links").asObject();

        if (links != null) {
            final JsonArray pacts = links.get("pacts").asArray();

            if (pacts != null) {
                return StreamSupport.stream(pacts.spliterator(), false)
                        .map(jsonNode -> jsonNode.asObject().getString("href", ""))
                        .map(URI::create)
                        .collect(toList());
            }
        }

        return new ArrayList<>();
    }

    public Callable<HttpResponse> getHttpResponseCallable() {
        return httpResponseCallable;
    }

    public void setHttpResponseCallable(Callable<HttpResponse> httpResponseCallable) {
        this.httpResponseCallable = httpResponseCallable;
    }

    static class PactBrokerImpl implements PactBroker {

        private static final String HOST = "host";
        private static final String PORT = "port";
        private static final String PROTOCOL = "protocol";
        private static final String TAGS = "tags";

        private String host = "";
        private String port = "";
        private String protocol = "http";
        private List<String> tags = new ArrayList<>();

        public PactBrokerImpl(Map<String, Object> configuration) {

            if (configuration.containsKey(HOST)) {
                this.host = (String) configuration.get(HOST);
            } else {
                throw new IllegalArgumentException("Host field is mandatory in Pact Broker Retriever.");
            }

            if (configuration.containsKey(PORT)) {
                this.port = (String) configuration.get(PORT);
            } else {
                throw new IllegalArgumentException("Port field is mandatory in Pact Broker Retriever.");
            }

            if (configuration.containsKey(PROTOCOL)) {
                this.protocol = (String) configuration.get(PROTOCOL);
            }

            if (configuration.containsKey(TAGS)) {
                Object tags = configuration.get(TAGS);

                if (tags instanceof String) {
                    this.tags.add(RunnerExpressionParser.parseExpressions((String) tags));
                } else {
                    if(tags instanceof Collection) {
                        Collection<String> listOfTags = (Collection) tags;
                        this.tags.addAll(listOfTags.stream().map(tag -> getResolvedValue(tag))
                                .collect(toList()));
                    }
                }
            } else {
                this.tags.add("latest");
            }
        }

        @Override
        public String host() {
            return host;
        }

        @Override
        public String port() {
            return port;
        }

        @Override
        public String protocol() {
            return protocol;
        }

        @Override
        public String[] tags() {
            return tags.toArray(new String[tags.size()]);
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return PactBroker.class;
        }
    }

    private static String asStringPreservingNewLines(InputStream response) {
        StringWriter logwriter = new StringWriter();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response));

            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                logwriter.write(line);
                logwriter.write(System.lineSeparator());
            }

            return logwriter.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getResolvedValue(String field) {
        return RunnerExpressionParser.parseExpressions(field);
    }
}