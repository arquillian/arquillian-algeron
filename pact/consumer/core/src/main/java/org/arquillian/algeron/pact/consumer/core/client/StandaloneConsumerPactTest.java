package org.arquillian.algeron.pact.consumer.core.client;

import org.arquillian.algeron.pact.consumer.core.AbstractConsumerPactTest;
import org.arquillian.algeron.pact.consumer.spi.PactVerification;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Subclass for managing pacts for standalone classes. Cannot be used the client one because it uses Deployment class which is only available in case of container as client/incontainer mode
 */
public class StandaloneConsumerPactTest extends AbstractConsumerPactTest {

    private static final Logger logger = Logger.getLogger(StandaloneConsumerPactTest.class.getName());

    public void testPact(@Observes EventContext<Test> testEventContext) throws Throwable {

        final Test event = testEventContext.getEvent();
        final TestClass testClass = event.getTestClass();

        final PactVerification pactVerification = event.getTestMethod().getAnnotation(PactVerification.class);

        if (pactVerification == null) {
            logger.log(Level.INFO,
                    String.format("Method %s is not annotated with %s annotation and it is going to be executed as normal junit test.", event.getTestMethod().getName(), PactVerification.class.getName()));
            testEventContext.proceed();
            return;
        }

        executeConsumerTest(testEventContext, testClass, pactVerification);
    }

}
