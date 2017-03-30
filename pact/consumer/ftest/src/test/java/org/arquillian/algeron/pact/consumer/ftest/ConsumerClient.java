package org.arquillian.algeron.pact.consumer.ftest;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;

import java.net.URL;

public class ConsumerClient {

    public ValidatableResponse get(URL url) {
        RestAssured.port = url.getPort();
        return RestAssured.get().then();
    }
}
