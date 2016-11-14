package org.arquillian.algeron.pact.provider.loader.git.it;

import au.com.dius.pact.model.Consumer;
import au.com.dius.pact.model.RequestResponseInteraction;
import com.github.restdriver.clientdriver.ClientDriverRequest;
import com.github.restdriver.clientdriver.ClientDriverRule;
import org.arquillian.algeron.pact.provider.core.httptarget.Target;
import org.arquillian.algeron.pact.provider.loader.git.PactGit;
import org.arquillian.algeron.pact.provider.spi.CurrentConsumer;
import org.arquillian.algeron.pact.provider.spi.CurrentInteraction;
import org.arquillian.algeron.pact.provider.spi.Provider;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponseAsBytes;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;

@RunWith(Arquillian.class)
@Provider("planets_provider")
@Ignore("Ignored because requires external site that might not be available")
@PactGit("https://github.com/lordofthejars/starwarspact.git")
public class StarWarsGitProviderTest {

    @ClassRule
    public static final ClientDriverRule embeddedService = new ClientDriverRule(8332);

    @CurrentConsumer
    Consumer consumer;

    @CurrentInteraction
    RequestResponseInteraction interaction;

    @ArquillianResource
    Target target;

    @BeforeClass
    public static void recordServerInteractions() {
        embeddedService.addExpectation(
                onRequestTo("/rest/planet/orbital/average")
                        .withMethod(ClientDriverRequest.Method.GET),
                giveResponse("1298.3", "text/plain").withStatus(200));

        embeddedService.addExpectation(
                onRequestTo("/rest/planet/orbital/biggest")
                        .withMethod(ClientDriverRequest.Method.GET),
                giveResponseAsBytes(StarWarsGitProviderTest.class.getResourceAsStream("/server.json"), "application/json").withStatus(200));

    }


    @Test
    public void validateProvider() {
        target.testInteraction(consumer.getName(), interaction);
    }

}