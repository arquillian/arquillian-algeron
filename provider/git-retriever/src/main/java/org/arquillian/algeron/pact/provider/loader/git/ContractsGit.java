package org.arquillian.algeron.pact.provider.loader.git;

import org.arquillian.algeron.provider.spi.retriever.ContractsSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to point to source of pacts for contract tests from git repository
 * All properties supports ${name:default} syntax
 *
 * @see ContractsGitLoader pact loader
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ContractsSource(ContractsGitLoader.class)
public @interface ContractsGit {

    /**
     * URL of remote git repository
     */
    String value();

    /**
     * Username to access to repository
     */
    String username() default "";

    /**
     * Password to access to repository
     */
    String password() default "";

    /**
     * Passphrase to access to private key
     */
    String passphrase() default "";

    /**
     * Location and name of the private key. By default ~/.ssh/id_rsa
     */
    String key() default "~/.ssh/id_rsa";

    /**
     * Directory where remote git repository is cloned. By default uses temp directory
     */
    String repository() default "";

    /**
     * In case contracts are stored inside an specific folder, you need to specify where are stored
     */
    String contractsDirectory() default "";

    /**
     * If you want to checkout an specific tag
     */

    String tag() default "";

    /**
     * If you want to change to an specific branch. By default is master.
     */
    String branch() default "master";

    /**
     * Sets the remote used in pull operation, by default uses "origin"
     */
    String remote() default "origin";
}
