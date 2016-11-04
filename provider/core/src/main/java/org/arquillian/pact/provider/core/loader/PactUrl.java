package org.arquillian.pact.provider.core.loader;

import org.arquillian.pact.provider.spi.loader.PactSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to point to source of pacts for contract tests
 * All properties supports ${name:default} syntax
 *
 * @see PactUrlLoader pact loader
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@PactSource(PactUrlLoader.class)
public @interface PactUrl {
    /**
     * @return a list of urls to pact files
     */
    String[] urls();
}
