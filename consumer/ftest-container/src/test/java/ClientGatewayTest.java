import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import org.arquillian.pact.consumer.ftest.ClientGateway;
import org.arquillian.pact.consumer.spi.Pact;
import org.arquillian.pact.consumer.spi.PactVerification;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
@Pact(provider="test_provider", consumer="test_consumer")
public class ClientGatewayTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class).addClasses(ClientGateway.class);
    }

    public PactFragment createFragment(PactDslWithProvider builder) {

        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");

        Map<String, Object> stateParams = new HashMap<>();
        stateParams.put("name", "Alexandra");

        return builder
                .given("test state", stateParams)
                .uponReceiving("ConsumerTest test interaction")
                .path("/")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(header)
                .body("{\"responsetest\": true, \"name\": \"harry\"}")
                .toFragment();
    }

    @EJB
    ClientGateway clientGateway;

    @Test
    @PactVerification("test_provider")
    public void should_return_message() throws IOException {
        assertThat(clientGateway.getMessage(), is("{\"responsetest\": true, \"name\": \"harry\"}"));
    }
}
