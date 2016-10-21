package org.arquillian.pact.consumer.core.publisher;


import org.arquillian.pact.consumer.core.PactConsumerConfiguration;
import org.arquillian.pact.consumer.core.PactReportDirectoryConfigurator;
import org.arquillian.pact.consumer.core.PublishPacts;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PublishPactsTest extends AbstractManagerTestBase {

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(PublishPacts.class);
        super.addExtensions(extensions);
    }

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_move_files_to_output_directory() throws IOException {

        try {
            final File origin = temporaryFolder.newFolder("origin");
            System.setProperty(PactReportDirectoryConfigurator.PACT_ROOT_DIR, origin.getAbsolutePath());

            final File output = temporaryFolder.newFolder("output");
            byte[] content = "Contract File".getBytes();
            Files.copy(new ByteArrayInputStream(content), new File(origin, "pact.txt").toPath());

            String config = "provider: folder\n" +
                    "outputFolder: " + output.getAbsolutePath() + "\n";

            final Map<String, String> params = new HashMap<>();
            params.put("publishContracts", "true");
            params.put("pactPublishConfiguration", config);

            final PactConsumerConfiguration pactConsumerConfiguration = PactConsumerConfiguration.fromMap(params);
            bind(ApplicationScoped.class, PactConsumerConfiguration.class, pactConsumerConfiguration);

            fire(new AfterSuite());

            assertThat(output).isDirectory();
            assertThat(new File(output, "pact.txt")).exists().hasContent("Contract File");

        } finally {
            System.clearProperty(PactReportDirectoryConfigurator.PACT_ROOT_DIR);
        }
    }

    @Test
    public void should_not_move_files_to_output_directory_if_no_publish_contracts() throws IOException {

        try {
            final File origin = temporaryFolder.newFolder("origin");
            System.setProperty(PactReportDirectoryConfigurator.PACT_ROOT_DIR, origin.getAbsolutePath());

            final File output = temporaryFolder.newFolder("output");
            byte[] content = "Contract File".getBytes();
            Files.copy(new ByteArrayInputStream(content), new File(origin, "pact.txt").toPath());

            String config = "provider: folder\n" +
                    "outputFolder: " + output.getAbsolutePath() + "\n";

            final Map<String, String> params = new HashMap<>();
            params.put("pactPublishConfiguration", config);

            final PactConsumerConfiguration pactConsumerConfiguration = PactConsumerConfiguration.fromMap(params);
            bind(ApplicationScoped.class, PactConsumerConfiguration.class, pactConsumerConfiguration);

            fire(new AfterSuite());

            assertThat(output).isDirectory();
            assertThat(new File(output, "pact.txt")).doesNotExist();

        } finally {
            System.clearProperty(PactReportDirectoryConfigurator.PACT_ROOT_DIR);
        }
    }

}
