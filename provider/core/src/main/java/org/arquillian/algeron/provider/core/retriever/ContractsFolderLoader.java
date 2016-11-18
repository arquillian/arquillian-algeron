package org.arquillian.algeron.provider.core.retriever;

import org.arquillian.algeron.configuration.RunnerExpressionParser;
import org.arquillian.algeron.provider.spi.retriever.ContractsRetriever;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Out-of-the-box implementation of {@link org.arquillian.algeron.provider.spi.retriever.ContractsRetriever}
 * that retrieve contracts from either a subfolder of project resource folder or a directory
 */
public class ContractsFolderLoader implements ContractsRetriever {
    private final String path;

    public ContractsFolderLoader(final File path) {
        this(path.getPath());
    }

    public ContractsFolderLoader(final String path) {
        this.path = path;
    }

    /**
     * @deprecated Use ContractsUrlLoader for URLs
     */
    @Deprecated
    public ContractsFolderLoader(final URL path) {
        this(path == null ? "" : path.getPath());
    }

    public ContractsFolderLoader(final ContractsFolder contractsFolder) {
        this(contractsFolder.value());
    }

    @Override
    public List<URI> retrieve() throws IOException {
        File rootDirectory =  resolvePath();
        return Arrays.stream(rootDirectory.listFiles())
                .map(file -> "file://" + file.getAbsolutePath())
                .map(URI::create)
                .collect(Collectors.toList());
    }

    private File resolvePath() {
        final String pathname = RunnerExpressionParser.parseExpressions(path);
        File file = new File(pathname);
        URL resourcePath = ContractsFolderLoader.class.getClassLoader().getResource(pathname);
        if (resourcePath != null) {
            file = new File(resourcePath.getPath());
        }
        return file;
    }

}
