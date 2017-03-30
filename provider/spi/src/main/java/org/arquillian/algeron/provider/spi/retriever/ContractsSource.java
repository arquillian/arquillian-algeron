package org.arquillian.algeron.provider.spi.retriever;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Point out which {@link ContractsRetriever} use for pact loading
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
public @interface ContractsSource {
    /**
     * @return {@link ContractsRetriever} class that will be used for pact loading
     * @see ContractsRetriever
     */
    Class<? extends ContractsRetriever> value();
}
