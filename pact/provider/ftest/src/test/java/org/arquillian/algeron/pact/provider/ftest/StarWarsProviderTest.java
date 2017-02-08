package org.arquillian.algeron.pact.provider.ftest;

import com.github.restdriver.clientdriver.ClientDriverRequest;
import com.github.restdriver.clientdriver.ClientDriverRule;
import org.arquillian.algeron.pact.provider.spi.Target;
import org.arquillian.algeron.pact.provider.spi.Provider;
import org.arquillian.algeron.pact.provider.spi.VerificationReports;
import org.arquillian.algeron.provider.core.retriever.ContractsFolder;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.restdriver.clientdriver.RestClientDriver.*;

@RunWith(Arquillian.class)
@Provider("planets_provider")
@ContractsFolder("pacts")
@VerificationReports(value = {"recorder"})
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
