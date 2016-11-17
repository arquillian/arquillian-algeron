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
     * @return
     */
    String value();

    /**
     * Username to access to repository
     * @return
     */
    String username() default "";

    /**
     * Password to access to repository
     * @return
     */
    String password() default "";

    /**
     * Passphrase to access to private key
     * @return
     */
    String passphrase() default "";

    /**
     * Location and name of the private key. By default ~/.ssh/id_rsa
     * @return
     */
    String key() default "~/.ssh/id_rsa";

    /**
     * Directory where remote git repository is cloned. By default uses temp directory
     * @return
     */
    String repository() default "";

    /**
     * In case pacts are stored inside an specific folder, you need to specify where are stored
     * @return
     */
    String contractsDirectory() default "";

    /**
     * If you want to checkout an specific tag
     * @return
     */

    String tag() default "";

    /**
     * If you want to change to an specific branch. By default is master.
     * @return
     */
    String branch() default "master";

    /**
     * Sets the remote used in pull operation, by default uses "origin"
     * @return
     */
    String remote() default "origin";

}
