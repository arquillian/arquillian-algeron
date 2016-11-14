package org.arquillian.algeron.pact.consumer.ftest;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import org.arquillian.algeron.pact.consumer.spi.Pact;
import org.arquillian.algeron.pact.consumer.spi.PactVerification;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(Arquillian.class)
public class ConsumerTest {

    @Pact(provider = "test_provider", consumer = "test_consumer")
    public PactFragment createFragment(PactDslWithProvider builder) {

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
                .body("{\"responsetest\": true, \"name\": \"harry\"}")
                .toFragment();
    }

    @Test
    @PactVerification("test_provider")
    public void runTest() throws IOException {
        new ConsumerClient().get().body("name", equalTo("harry"));
    }

}
