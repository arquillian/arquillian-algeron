package org.arquillian.algeron.provider.core.retriever;

import org.arquillian.algeron.configuration.RunnerExpressionParser;
import org.arquillian.algeron.provider.spi.retriever.ContractsRetriever;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Implementation of {@link org.arquillian.algeron.provider.spi.retriever.ContractsRetriever} that download contracts from given urls
 */
public class ContractsUrlLoader implements ContractsRetriever {
    private final List<URI> urls;

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

    private static String getResolvedValue(String field) {
        return RunnerExpressionParser.parseExpressions(field);
    }
}