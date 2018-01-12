package org.arquillian.algeron.pact.consumer.ftest;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import org.arquillian.algeron.consumer.StubServer;
import org.arquillian.algeron.pact.consumer.spi.Pact;
import org.arquillian.algeron.pact.consumer.spi.PactVerification;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(Arquillian.class)
public class ConsumerTest {

    @Pact(provider = "test_provider", consumer = "test_consumer")
    public RequestResponsePact createFragment(PactDslWithProvider builder) {

        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");

        return builder
            .given("test state")
            .uponReceiving("ConsumerTest test interaction")
            .path("/")
            .method("GET")
            .willRespondWith()
            .status(200)
            .headers(header)
            .bodyWithSingleQuotes(("{'responsetest': true, 'name': 'harry'}"))
            .toPact();
    }

    @StubServer
    URL url;

    @Test
    @PactVerification("test_provider")
    public void runTest() {
        new ConsumerClient().get(url).body("name", equalTo("harry"));
    }
}
