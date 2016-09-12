package org.arquillian.pact.provider.ftest;

import au.com.dius.pact.model.Consumer;
import au.com.dius.pact.model.RequestResponseInteraction;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.loader.PactFolder;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import com.github.restdriver.clientdriver.ClientDriverRequest;
import com.github.restdriver.clientdriver.ClientDriverRule;
import org.arquillian.pact.provider.core.target.HttpTarget;
import org.arquillian.pact.provider.spi.CurrentConsumer;
import org.arquillian.pact.provider.spi.CurrentInteraction;
import org.jboss.arquillian.junit.Arquillian;
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

    @TestTarget
    Target target = new HttpTarget(8332);

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
