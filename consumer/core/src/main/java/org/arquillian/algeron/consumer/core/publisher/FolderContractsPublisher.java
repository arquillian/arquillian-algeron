package org.arquillian.algeron.consumer.core.publisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;
import org.arquillian.algeron.configuration.HomeResolver;
import org.arquillian.algeron.configuration.RunnerExpressionParser;
import org.arquillian.algeron.consumer.spi.publisher.ContractsPublisher;

public class FolderContractsPublisher implements ContractsPublisher {

    private static final String OUTPUT_FOLDER = "outputFolder";
    private static final String CONTRACTS_FOLDER = "contractsFolder";
    private static final String DELETE_FOLDER = "deleteFolder";

    private Map<String, Object> configuration = null;

    @Override
    public void publish() throws IOException {
        final String path = (String) this.configuration.get(OUTPUT_FOLDER);
        final Path outputPath = resolveHomeDirectory(Paths.get(RunnerExpressionParser.parseExpressions(path)));

        if (shouldDeleteFolderDirectory()) {
            deleteDirectory(outputPath);
        }

        if (Files.notExists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        final String contractFolder = (String) this.configuration.get(CONTRACTS_FOLDER);
        final Path contractsSource = Paths.get(RunnerExpressionParser.parseExpressions(contractFolder));
        copyPactFiles(contractsSource, outputPath);
    }

    private boolean shouldDeleteFolderDirectory() {
        return this.configuration.containsKey(DELETE_FOLDER) && Boolean.parseBoolean(
            (String) this.configuration.get(DELETE_FOLDER));
    }

    protected void copyPactFiles(Path pactsLocation, Path outputPath) throws IOException {
        try (Stream<Path> stream = Files.walk(pactsLocation)) {
            stream.forEach(path -> {
                try {
                    if (!Files.isDirectory(path)) {
                        final Path pactFile = outputPath.resolve(path.getFileName());
                        Files.copy(path, pactFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            });
        }
    }

    private Path resolveHomeDirectory(Path path) {
        if (path.startsWith("~")) {
            return Paths.get(HomeResolver.resolveHomeDirectory(path.toString()));
        }

        return path;
    }

    private void deleteDirectory(Path path) {
        try {
            if (Files.exists(path)) {
                Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Couldn't delete folder directory for contracts", e);
        }
    }

    @Override
    public String getName() {
        return "folder";
    }

    @Override
    public void configure(Map<String, Object> configuration) {
        this.configuration = configuration;

        if (!this.configuration.containsKey(OUTPUT_FOLDER)) {
            throw new IllegalArgumentException(
                String.format("Folder Publisher requires %s configuration property", OUTPUT_FOLDER));
        }

        if (!(this.configuration.get(OUTPUT_FOLDER) instanceof String)) {
            throw new IllegalArgumentException(
                String.format("Folder Publisher requires %s configuration property to be an String", OUTPUT_FOLDER));
        }

        if (!this.configuration.containsKey(CONTRACTS_FOLDER)) {
            throw new IllegalArgumentException(
                String.format("Folder Publisher requires %s configuration property", CONTRACTS_FOLDER));
        }

        if (!(this.configuration.get(CONTRACTS_FOLDER) instanceof String)) {
            throw new IllegalArgumentException(
                String.format("Folder Publisher requires %s configuration property to be an String", CONTRACTS_FOLDER));
        }
    }
}
