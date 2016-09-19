package org.arquillian.pact.consumer.core;

import org.jboss.arquillian.container.test.spi.command.Command;

import java.io.Serializable;

public class PactFilesCommand implements Command<String>, Serializable {

    private String result;
    private Throwable failure;

    private String name;
    private byte[] content;

    public PactFilesCommand(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public void setThrowable(Throwable throwable) {
        this.failure = throwable;
    }

    @Override
    public Throwable getThrowable() {
        return this.failure;
    }

    public byte[] getContent() {
        return content;
    }

    public String getName() {
        return name;
    }
}
