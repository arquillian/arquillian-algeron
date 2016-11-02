package org.arquillian.pact.consumer.core.publisher;


import net.jcip.annotations.NotThreadSafe;
import org.arquillian.pact.consumer.core.PactConsumerConfiguration;
import org.arquillian.pact.consumer.core.PactReportDirectoryConfigurator;
import org.arquillian.pact.consumer.core.PactsPublisher;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.junit.After;
import org.junit.Before;
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

@NotThreadSafe
public class FolderPactsPublisherTest extends AbstractManagerTestBase {

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(PactsPublisher.class);
        super.addExtensions(extensions);
    }

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Map<String, String> params;
    private File output;

    @Before
    public void setupConfiguration() throws IOException {
        final File origin = temporaryFolder.newFolder("origin");
        System.setProperty(PactReportDirectoryConfigurator.PACT_ROOT_DIR, origin.getAbsolutePath());

        output = temporaryFolder.newFolder("output");
        byte[] content = "Contract File".getBytes();
        Files.copy(new ByteArrayInputStream(content), new File(origin, "pact.txt").toPath());

        String config = "provider: folder\n" +
                "outputFolder: " + output.getAbsolutePath() + "\n";

        params = new HashMap<>();
        params.put("pactPublishConfiguration", config);
    }

    @After
    public void unsetSystemProperties() {
        System.clearProperty(PactReportDirectoryConfigurator.PACT_ROOT_DIR);
    }

    @Test
    public void should_move_files_to_output_directory() throws IOException {

        params.put("publishContracts", "true");
        final PactConsumerConfiguration pactConsumerConfiguration = PactConsumerConfiguration.fromMap(params);
        bind(ApplicationScoped.class, PactConsumerConfiguration.class, pactConsumerConfiguration);

        fire(new AfterSuite());

        assertThat(output).isDirectory();
        assertThat(new File(output, "pact.txt")).exists().hasContent("Contract File");

    }

    @Test
    public void should_not_move_files_to_output_directory_if_no_publish_contracts() throws IOException {


        final PactConsumerConfiguration pactConsumerConfiguration = PactConsumerConfiguration.fromMap(params);
        bind(ApplicationScoped.class, PactConsumerConfiguration.class, pactConsumerConfiguration);

        fire(new AfterSuite());

        assertThat(output).isDirectory();
        assertThat(new File(output, "pact.txt")).doesNotExist();

    }

}
