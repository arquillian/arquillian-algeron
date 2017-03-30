package org.arquillian.algeron.provider.core.retriever;

import org.arquillian.algeron.provider.spi.retriever.ContractsSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to point to source of pacts for contract tests
 * All properties supports ${name:default} syntax
 *
 * @see ContractsFolderLoader pact loader
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ContractsSource(ContractsFolderLoader.class)
public @interface ContractsFolder {
    /**
     * @return path to subfolder of project resource folder with pact
     */
    String value();
}