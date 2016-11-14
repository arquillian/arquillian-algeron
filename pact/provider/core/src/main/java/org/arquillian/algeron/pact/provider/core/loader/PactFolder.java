package org.arquillian.algeron.pact.provider.core.loader;


import org.arquillian.algeron.pact.provider.spi.loader.PactSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to point to source of pacts for contract tests
 * All properties supports ${name:default} syntax
 *
 * @see PactFolderLoader pact loader
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@PactSource(PactFolderLoader.class)
public @interface PactFolder {
    /**
     * @return path to subfolder of project resource folder with pact
     */
    String value();
}