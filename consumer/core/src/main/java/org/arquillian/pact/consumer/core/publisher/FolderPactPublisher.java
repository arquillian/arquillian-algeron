package org.arquillian.pact.consumer.core.publisher;

import org.arquillian.pact.common.configuration.PactRunnerExpressionParser;
import org.arquillian.pact.consumer.spi.publisher.PactPublisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.stream.Stream;

public class FolderPactPublisher implements PactPublisher {

    private static final String OUTPUT_FOLDER = "outputFolder";

    private Map<String, Object> configuration = null;

    @Override
    public void store(final Path pactsLocation) {
        final String path = (String) this.configuration.get(OUTPUT_FOLDER);
        final Path outputPath = Paths.get(PactRunnerExpressionParser.parseExpressions(path));

        try {

            if (Files.notExists(outputPath)) {
                Files.createDirectories(outputPath);
            }

            copyPactFiles(pactsLocation, outputPath);

        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

    }

    protected void copyPactFiles(Path pactsLocation, Path outputPath) throws IOException {
        try (Stream<Path> stream = Files.walk(pactsLocation)) {
            stream.forEach(path -> {
                try {
                    if (! Files.isDirectory(path)) {
                        final Path pactFile = outputPath.resolve(path.getFileName());
                        Files.copy(path, pactFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            });
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
            throw new IllegalArgumentException(String.format("Folder Pact Publisher requires %s configuration property", OUTPUT_FOLDER));
        }

        if (!(this.configuration.get(OUTPUT_FOLDER) instanceof String)) {
            throw new IllegalArgumentException(String.format("Folder Pact Publisher requires %s configuration property to be an String", OUTPUT_FOLDER));
        }
    }
}
