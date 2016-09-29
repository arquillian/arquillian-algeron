package org.arquillian.pact.provider.ftest;

import com.github.restdriver.clientdriver.ClientDriverRequest;
import com.github.restdriver.clientdriver.ClientDriverRule;
import org.arquillian.pact.provider.core.httptarget.Target;
import org.arquillian.pact.provider.core.loader.PactFolder;
import org.arquillian.pact.provider.spi.Provider;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.restdriver.clientdriver.RestClientDriver.*;

@RunWith(Arquillian.class)
@Provider("planets_provider")
@PactFolder("pacts")
public class StarWarsProviderTest {

    @ClassRule
    public static final ClientDriverRule embeddedService = new ClientDriverRule(8332);

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
        target.testInteraction();
    }

}
