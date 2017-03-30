package org.arquillian.algeron.pact.consumer.core.client;

import org.arquillian.algeron.pact.consumer.core.AbstractConsumerPactTest;
import org.arquillian.algeron.pact.consumer.spi.PactVerification;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.test.impl.RunModeUtils;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsumerPactTest extends AbstractConsumerPactTest {

    private static final Logger logger = Logger.getLogger(ConsumerPactTest.class.getName());

    public void testPact(@Observes(precedence = -50) EventContext<Test> testEventContext, Deployment deployment)
        throws Throwable {

        final Test event = testEventContext.getEvent();
        final TestClass testClass = event.getTestClass();

        // We need to check this because in case of embedded containers this class is executed too
        if (RunModeUtils.isRunAsClient(deployment, testClass, event.getTestMethod())) {

            final PactVerification pactVerification = event.getTestMethod().getAnnotation(PactVerification.class);

            if (pactVerification == null) {
                logger.log(Level.INFO,
                    String.format(
                        "Method %s is not annotated with %s annotation and it is going to be executed as normal junit test.",
                        event.getTestMethod().getName(), PactVerification.class.getName()));
                testEventContext.proceed();
                return;
            }

            executeConsumerTest(testEventContext, testClass, pactVerification);
        } else {
            // We are in container and this class is executed in client side so we should only pass the execution and incontainer class will do the job
            testEventContext.proceed();
        }
    }
}
