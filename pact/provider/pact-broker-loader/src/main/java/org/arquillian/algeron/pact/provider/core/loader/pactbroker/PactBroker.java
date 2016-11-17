package org.arquillian.algeron.pact.provider.core.loader.pactbroker;

import org.arquillian.algeron.provider.spi.retriever.ContractsSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to point to source of pacts for contract tests
 * All properties supports ${name:-default} syntax
 *
 * @see PactBrokerLoader pact loader
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ContractsSource(PactBrokerLoader.class)
public @interface PactBroker {
    /**
     * @return host of pact broker
     */
    String host();

    /**
     * @return port of pact broker
     */
    String port();

    /**
     * HTTP protocol, defaults to http
     */
    String protocol() default "http";

    /**
     * Tags to use to fetch pacts for
     */
    String[] tags() default "latest";
}

