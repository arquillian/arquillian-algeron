package org.arquillian.algeron.pact.consumer.ftest;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;


public class ConsumerClient {

    public ValidatableResponse get() {
        RestAssured.port = 9090;
        return RestAssured.get().then();
    }
}
