import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import au.com.dius.pact.model.RequestResponsePact;
import org.arquillian.algeron.consumer.StubServer;
import org.arquillian.algeron.pact.consumer.ftest.ClientGateway;
import org.arquillian.algeron.pact.consumer.spi.Pact;
import org.arquillian.algeron.pact.consumer.spi.PactVerification;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
@Pact(provider = "test_provider", consumer = "test_consumer")
public class ClientGatewayTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class).addClasses(ClientGateway.class);
    }

    public RequestResponsePact createFragment(PactDslWithProvider builder) {

        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");

        return builder
            .given("test state", "name", "Alexandra")
            .uponReceiving("ConsumerTest test interaction")
            .path("/")
            .method("GET")
            .willRespondWith()
            .status(200)
            .headers(header)
            .bodyWithSingleQuotes("{'responsetest': true, 'name': 'harry'}")
            .toPact();
    }

    @EJB
    ClientGateway clientGateway;

    @StubServer
    URL url;

    @Test
    @PactVerification("test_provider")
    public void should_return_message() throws IOException {
        assertThat(clientGateway.getMessage(url), is("{\"responsetest\":true,\"name\":\"harry\"}"));
    }
}
