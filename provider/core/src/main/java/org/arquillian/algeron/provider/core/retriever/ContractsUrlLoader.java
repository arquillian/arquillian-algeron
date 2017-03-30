package org.arquillian.algeron.provider.core.retriever;

import org.arquillian.algeron.configuration.RunnerExpressionParser;
import org.arquillian.algeron.provider.spi.retriever.ContractsRetriever;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Implementation of {@link org.arquillian.algeron.provider.spi.retriever.ContractsRetriever} that download contracts from
 * given urls
 */
public class ContractsUrlLoader implements ContractsRetriever {

    private static final String URL = "url";

    private List<URI> urls;

    public ContractsUrlLoader() {
    }

    public ContractsUrlLoader(final List<URI> urls) {
        this.urls = urls;
    }

    public ContractsUrlLoader(final ContractsUrl contractsUrl) {
        this(Arrays.stream(contractsUrl.urls())
            .map(url -> getResolvedValue(url))
            .map(URI::create)
            .collect(toList()));
    }

    @Override
    public List<URI> retrieve() throws IOException {
        return urls;
    }

    @Override
    public String getName() {
        return "url";
    }

    @Override
    public void configure(Map<String, Object> configuration) {
        if (!configuration.containsKey(URL)) {
            throw new IllegalArgumentException(String.format("Url Retriever requires %s configuration property", URL));
        }

        final Object url = configuration.get(URL);
        if (!(url instanceof String || url instanceof Collection)) {
            throw new IllegalArgumentException(
                String.format("Url Retriever requires %s configuration property to be an String or List of Strings",
                    URL));
        }

        if (url instanceof String) {
            this.urls = new ArrayList<>();
            this.urls.add(URI.create((String) url));
        } else {
            this.urls = ((List<String>) url).stream()
                .map(u -> getResolvedValue(u))
                .map(URI::create)
                .collect(toList());
        }
    }

    private static String getResolvedValue(String field) {
        return RunnerExpressionParser.parseExpressions(field);
    }
}