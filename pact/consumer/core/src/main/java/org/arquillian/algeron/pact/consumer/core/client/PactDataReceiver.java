package org.arquillian.algeron.pact.consumer.core.client;

import org.arquillian.algeron.pact.consumer.core.PactFilesCommand;
import org.jboss.arquillian.core.api.annotation.Observes;

import java.io.File;
import java.nio.file.Files;

public class PactDataReceiver {

    public void storePactData(@Observes PactFilesCommand pactFilesCommand) {

        try {

            final String name = pactFilesCommand.getName();
            final byte[] content = pactFilesCommand.getContent();

            File output = new File(getDestination(), name);
            Files.write(output.toPath(), content);

            pactFilesCommand.setResult("SUCCESS");
        } catch (Exception e) {
            pactFilesCommand.setResult("FAILURE");
            pactFilesCommand.setThrowable(e);
        }
    }

    private String getDestination() {
        return System.getProperty("pact.rootDir", "target/pacts");
    }
}
