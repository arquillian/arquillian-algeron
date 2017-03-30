package org.arquillian.algeron.pact.consumer.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * describes the interactions between a provider and a consumer.
 * The annotated method has to be of following signature:
 * <p>
 * public PactFragment providerDef1(PactDslWithProvider builder) {...}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Pact {

    /**
     * name of the provider
     */
    String provider();

    /**
     * name of the consumer
     */
    String consumer();

}
