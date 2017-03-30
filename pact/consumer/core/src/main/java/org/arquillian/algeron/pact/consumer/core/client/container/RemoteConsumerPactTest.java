package org.arquillian.algeron.pact.consumer.core.client.container;

import org.arquillian.algeron.pact.consumer.core.AbstractConsumerPactTest;
import org.arquillian.algeron.pact.consumer.core.PactFilesCommand;
import org.arquillian.algeron.pact.consumer.spi.PactVerification;
import org.jboss.arquillian.container.test.spi.command.CommandService;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoteConsumerPactTest extends AbstractConsumerPactTest {

    @Inject
    Instance<ServiceLoader> serviceLoader;

    private static final Logger logger = Logger.getLogger(RemoteConsumerPactTest.class.getName());

    public void testPact(@Observes(precedence = -50) EventContext<Test> testEventContext) throws Throwable {

        final Test event = testEventContext.getEvent();
        final TestClass testClass = event.getTestClass();

        final PactVerification pactVerification = event.getTestMethod().getAnnotation(PactVerification.class);

        if (pactVerification == null) {
            logger.log(Level.INFO,
                String.format(
                    "Method %s is not annotated with %s annotation and it is going to be executed as normal junit test.",
                    event.getTestMethod().getName(), PactVerification.class.getName()));
            testEventContext.proceed();
            return;
        }

        final ConsumerProviderPair consumerProviderPair =
            executeConsumerTest(testEventContext, testClass, pactVerification);

        // Send results back to client
        final String filename = getFilename(consumerProviderPair);
        final byte[] content = loadPact(filename);

        getCommandService().execute(new PactFilesCommand(filename, content));
    }

    private byte[] loadPact(String filename) throws IOException {
        //By default pact stores pacts in this directory and in case of managed/remote mode always will be this path on remote side
        File pact = new File("target/pacts", filename);
        return Files.readAllBytes(pact.toPath());
    }

    private String getFilename(ConsumerProviderPair consumerProviderPair) {
        return consumerProviderPair.getConsumer() + "-" + consumerProviderPair.getProvider() + ".json";
    }

    private CommandService getCommandService() {
        ServiceLoader loader = serviceLoader.get();
        if (loader == null) {
            throw new IllegalStateException("No " + ServiceLoader.class.getName() + " found in context");
        }
        CommandService service = loader.onlyOne(CommandService.class);
        if (service == null) {
            throw new IllegalStateException("No " + CommandService.class.getName() + " found in context");
        }
        return service;
    }
}
