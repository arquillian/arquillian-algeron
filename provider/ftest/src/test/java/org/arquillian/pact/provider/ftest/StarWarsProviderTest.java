package org.arquillian.pact.provider.ftest;

import au.com.dius.pact.model.Consumer;
import au.com.dius.pact.model.RequestResponseInteraction;
import com.github.restdriver.clientdriver.ClientDriverRequest;
import com.github.restdriver.clientdriver.ClientDriverRule;
import org.arquillian.pact.provider.core.loader.PactFolder;
import org.arquillian.pact.provider.core.target.Target;
import org.arquillian.pact.provider.spi.CurrentConsumer;
import org.arquillian.pact.provider.spi.CurrentInteraction;
import org.arquillian.pact.provider.spi.Provider;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponseAsBytes;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;

@RunWith(Arquillian.class)
@Provider("planets_provider")
@PactFolder("pacts")
public class StarWarsProviderTest {

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
                giveResponseAsBytes(StarWarsProviderTest.class.getResourceAsStream("/server.json"), "application/json").withStatus(200));

    }


    @Test
    public void validateProvider() {
        target.testInteraction(consumer.getName(), interaction);
    }

}
