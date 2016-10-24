package org.arquillian.pact.provider.spi.loader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Point out which {@link PactLoader} use for pact loading
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
public @interface PactSource {
    /**
     * @return {@link PactLoader} class that will be used for pact loading
     *
     * @see PactLoader
     */
    Class<? extends PactLoader> value();
}
