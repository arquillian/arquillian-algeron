package org.arquillian.algeron.consumer.core.publisher;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import net.jcip.annotations.NotThreadSafe;
import org.arquillian.algeron.consumer.core.AlgeronConsumerConfiguration;
import org.arquillian.algeron.consumer.core.ContractsPublisherObserver;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
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
public class UrlContractsPublisherObserverTest extends AbstractManagerTestBase {

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(ContractsPublisherObserver.class);
        super.addExtensions(extensions);
    }

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public WireMockRule service = new WireMockRule(18081);

    @Test
    public void should_move_files_to_output_directory() throws IOException {

        final File origin = temporaryFolder.newFolder("origin");

        byte[] content = "Contract File".getBytes();
        Files.copy(new ByteArrayInputStream(content), new File(origin, "pact.txt").toPath());

        String config = "provider: url\n" +
            "contractsFolder: " + origin.getAbsolutePath() + "\n" +
            "url: http://localhost:18081/pacts\n";

        final Map<String, String> params = new HashMap<>();
        params.put("publishContracts", "true");
        params.put("publishConfiguration", config);

        final AlgeronConsumerConfiguration pactConsumerConfiguration = AlgeronConsumerConfiguration.fromMap(params);
        bind(ApplicationScoped.class, AlgeronConsumerConfiguration.class, pactConsumerConfiguration);

        WireMock.stubFor(
            WireMock.post(WireMock.urlEqualTo("/pacts/pact.txt"))
                .withRequestBody(new ContainsPattern("Contract File"))
                .willReturn(WireMock.aResponse().withStatus(200)));

        fire(new AfterClass(UrlContractsPublisherObserverTest.class));

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/pacts/pact.txt"))
            .withRequestBody(new ContainsPattern("Contract File")));
    }
}
