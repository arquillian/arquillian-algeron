package org.arquillian.algeron.provider.core.retriever;

import org.arquillian.algeron.configuration.HomeResolver;
import org.arquillian.algeron.configuration.RunnerExpressionParser;
import org.arquillian.algeron.provider.spi.retriever.ContractsRetriever;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Out-of-the-box implementation of {@link org.arquillian.algeron.provider.spi.retriever.ContractsRetriever}
 * that retrieve contracts from either a subfolder of project resource folder or a directory
 */
public class ContractsFolderLoader implements ContractsRetriever {

    private static final String CONTRACTS_FOLDER = "contractsFolder";

    private String path;

    public ContractsFolderLoader() {
    }

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
    public List<URI> retrieve() {
        File rootDirectory = resolvePath();
        return Arrays.stream(rootDirectory.listFiles())
            .map(File::toURI)
            .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return "folder";
    }

    @Override
    public void configure(Map<String, Object> configuration) {
        if (!configuration.containsKey(CONTRACTS_FOLDER)) {
            throw new IllegalArgumentException(
                String.format("Folder Retriever requires %s configuration property", CONTRACTS_FOLDER));
        }

        if (!(configuration.get(CONTRACTS_FOLDER) instanceof String)) {
            throw new IllegalArgumentException(
                String.format("Folder Retriever requires %s configuration property to be an String", CONTRACTS_FOLDER));
        }

        this.path = (String) configuration.get(CONTRACTS_FOLDER);
    }

    private File resolvePath() {
        final String pathname = resolveHomeDirectory(RunnerExpressionParser.parseExpressions(path));

        File file = new File(pathname);

        URL resourcePath = ContractsFolderLoader.class.getClassLoader().getResource(pathname);
        if (resourcePath != null) {
            file = new File(resourcePath.getPath());
        }

        return file;
    }

    private String resolveHomeDirectory(String path) {
        if (path != null && path.startsWith("~")) {
            return HomeResolver.resolveHomeDirectory(path);
        }

        return path;
    }
}
