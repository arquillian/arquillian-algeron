package org.arquillian.algeron.pact.provider.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface VerificationReports {

    /**
     * Names of the reports to generate
     */
    String[] value() default "console";

    /**
     * Directory where reports should be written
     */
    String reportDir() default "target/pact/reports";
}
