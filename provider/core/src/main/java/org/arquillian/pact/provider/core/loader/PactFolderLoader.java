package org.arquillian.pact.provider.core.loader;

import au.com.dius.pact.model.Pact;
import au.com.dius.pact.model.PactReader;
import org.arquillian.pact.provider.api.PactRunnerExpressionParser;
import org.arquillian.pact.provider.spi.loader.PactLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Out-of-the-box implementation of {@link PactLoader}
 * that loads pacts from either a subfolder of project resource folder or a directory
 */
public class PactFolderLoader implements PactLoader {
    private final String path;

    public PactFolderLoader(final File path) {
        this(path.getPath());
    }

    public PactFolderLoader(final String path) {
        this.path = path;
    }

    /**
     * @deprecated Use PactUrlLoader for URLs
     */
    @Deprecated
    public PactFolderLoader(final URL path) {
        this(path == null ? "" : path.getPath());
    }

    public PactFolderLoader(final PactFolder pactFolder) {
        this(pactFolder.value());
    }

    @Override
    public List<Pact> load(final String providerName) throws IOException {
        List<Pact> pacts = new ArrayList<Pact>();
        File pactFolder = resolvePath();

        File[] files = pactFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            return Arrays.stream(files)
                    .map(PactReader::loadPact)
                    .filter(pact -> pact.getProvider().getName().equals(providerName))
                    .collect(Collectors.toList());
        }

        return pacts;
    }

    private File resolvePath() {
        final String pathname = PactRunnerExpressionParser.parseExpressions(path);
        File file = new File(pathname);
        URL resourcePath = PactFolderLoader.class.getClassLoader().getResource(pathname);
        if (resourcePath != null) {
            file = new File(resourcePath.getPath());
        }
        return file;
    }
}
