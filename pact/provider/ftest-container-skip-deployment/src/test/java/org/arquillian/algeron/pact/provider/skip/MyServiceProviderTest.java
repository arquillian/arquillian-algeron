package org.arquillian.algeron.pact.provider.skip;

import com.github.restdriver.clientdriver.ClientDriverRequest;
import com.github.restdriver.clientdriver.ClientDriverRule;
import org.arquillian.algeron.pact.provider.core.httptarget.Target;
import org.arquillian.algeron.pact.provider.spi.Provider;
import org.arquillian.algeron.pact.provider.spi.State;
import org.arquillian.algeron.provider.api.deployment.Environment;
import org.arquillian.algeron.provider.core.retriever.ContractsFolder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.Map;

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponseAsBytes;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
@Provider("test_provider")
@ContractsFolder("pacts")
@RunAsClient
public class MyServiceProviderTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addClass(MyService.class);
    }

    @ArquillianResource
    @Environment("myservice.url")
    URL webapp;

    @ArquillianResource
    Target target;

    @ClassRule
    public static final ClientDriverRule embeddedService = new ClientDriverRule(8332);

    @BeforeClass
    public static void recordServerInteractions() {

        embeddedService.addExpectation(
                onRequestTo("/")
                        .withMethod(ClientDriverRequest.Method.GET),
                giveResponseAsBytes(MyServiceProviderTest.class.getResourceAsStream("/server.json"), "application/json").withStatus(200));

    }

    @State("test state")
    public void testStateMethod(Map<String, Object> params) {
        assertThat(params).containsEntry("name", "Alexandra");
    }

    @Test
    public void should_provide_valid_answers() {
        target.testInteraction(webapp);
    }

}
