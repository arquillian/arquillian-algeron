package org.arquillian.algeron.consumer.core.publisher;


import net.jcip.annotations.NotThreadSafe;
import org.arquillian.algeron.consumer.core.AlgeronConsumerConfiguration;
import org.arquillian.algeron.consumer.core.ContractsPublisher;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
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

@NotThreadSafe
public class FolderContractsPublisherTest extends AbstractManagerTestBase {

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(ContractsPublisher.class);
        super.addExtensions(extensions);
    }

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Map<String, String> params;
    private File output;

    @Before
    public void setupConfiguration() throws IOException {
        final File origin = temporaryFolder.newFolder("origin");

        output = temporaryFolder.newFolder("output");
        byte[] content = "Contract File".getBytes();
        Files.copy(new ByteArrayInputStream(content), new File(origin, "pact.txt").toPath());

        String config = "provider: folder\n" +
                "contractsFolder: " + origin.getAbsolutePath() + "\n" +
                "outputFolder: " + output.getAbsolutePath() + "\n";

        params = new HashMap<>();
        params.put("publishConfiguration", config);
    }

    @Test
    public void should_move_files_to_output_directory() throws IOException {

        params.put("publishContracts", "true");
        final AlgeronConsumerConfiguration pactConsumerConfiguration = AlgeronConsumerConfiguration.fromMap(params);
        bind(ApplicationScoped.class, AlgeronConsumerConfiguration.class, pactConsumerConfiguration);

        fire(new AfterClass(FolderContractsPublisherTest.class));

        Assertions.assertThat(output).isDirectory();
        Assertions.assertThat(new File(output, "pact.txt")).exists().hasContent("Contract File");

    }

    @Test
    public void should_not_move_files_to_output_directory_if_no_publish_contracts() throws IOException {

        final AlgeronConsumerConfiguration pactConsumerConfiguration = AlgeronConsumerConfiguration.fromMap(params);
        bind(ApplicationScoped.class, AlgeronConsumerConfiguration.class, pactConsumerConfiguration);

        fire(new AfterClass(FolderContractsPublisherTest.class));

        Assertions.assertThat(output).isDirectory();
        Assertions.assertThat(new File(output, "pact.txt")).doesNotExist();

    }

}
