package org.arquillian.algeron.pact.consumer.core.client.container;

import org.arquillian.algeron.pact.consumer.core.PactConsumerConfiguration;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class PactConsumerConfigurator {

    @Inject
    @ApplicationScoped
    InstanceProducer<PactConsumerConfiguration> pactConsumerConfigurationInstanceProducer;

    public void configure(@Observes(precedence = 50) BeforeClass beforeClass) throws IOException {

        final InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/pact-consumer-configuration.properties");
        Properties properties = new Properties();
        properties.load(resourceAsStream);

        final Map<String, String> config = properties.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
        pactConsumerConfigurationInstanceProducer.set(PactConsumerConfiguration.fromMap(config));
    }

}
