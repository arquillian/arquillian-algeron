package org.arquillian.algeron.pact.provider.loader.maven;


import org.arquillian.algeron.pact.provider.spi.loader.PactSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to point to source of pacts for contract tests
 * All properties supports ${name:-default} syntax
 *
 * @see PactMavenDependencyLoader pact loader
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@PactSource(PactMavenDependencyLoader.class)
public @interface PactMavenDependency {

    /**
     * List of Maven coordinates in form of group:artifact:(package:classifier:)version
     *
     * version can be specified as http://maven.apache.org/enforcer/enforcer-rules/versionRanges.html and loader will use the highest version
     *
     * @return List of maven coordinates
     */
    String[] value() default "";

    /**
     * If Maven should run offline
     * @return True if offline.
     */
    boolean offline() default false;

    /**
     * Sets classpath location of a settings.xml file
     * @return Classptah location of a settings.xml to be used during the artifact resolution
     */
    String customSettings() default "";

    /**
     * Sets a remote repository to be used instead of ones defined in settings.xml or default ones in form of name:url:layout
     * @return remote repository.
     */
    String remoteRepository() default "";

}
