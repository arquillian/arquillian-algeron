package org.arquillian.pact.provider.core.loader;

import au.com.dius.pact.model.Pact;
import au.com.dius.pact.model.PactReader;
import org.arquillian.pact.configuration.PactRunnerExpressionParser;
import org.arquillian.pact.provider.spi.loader.PactLoader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Implementation of {@link PactLoader} that downloads pacts from given urls
 */
public class PactUrlLoader implements PactLoader {
    private final List<String> urls;

    public PactUrlLoader(final List<String> urls) {
        this.urls = urls;
    }

    public PactUrlLoader(final PactUrl pactUrl) {
        this(Arrays.stream(pactUrl.urls())
                .map( url -> getResolvedValue(url))
                .collect(toList()));
    }

    public List<Pact> load(final String providerName) throws IOException {
        return urls.stream()
                .map(PactReader::loadPact)
                .map(obj -> (Pact) obj)
                .collect(toList());
    }

    private static String getResolvedValue(String field) {
        return PactRunnerExpressionParser.parseExpressions(field);
    }
}