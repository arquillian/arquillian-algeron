package org.arquillian.algeron.pact.consumer.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Before each test, a mock server will be setup at given port/host that will provide mocked responses.
 * after each test, it will be teared down.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PactVerification {

    /**
     * the tested provider name.
     */
    String value() default "";

    /**
     * Method to call to get the pact fragment. Defaults to empty string which results in using the first one found.
     * This should be filled with the name of the method annotated with `@Pact` that want to be executed.
     */
    String fragment() default "";
}
