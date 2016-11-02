package org.arquillian.pact.consumer.core.client;

import org.arquillian.pact.consumer.core.AbstractConsumerPactTest;
import org.arquillian.pact.consumer.core.PactConsumerConfiguration;
import org.arquillian.pact.consumer.core.PactFilesCommand;
import org.arquillian.pact.consumer.core.client.container.ConsumerProviderPair;
import org.arquillian.pact.consumer.core.client.container.PactConsumerConfigurator;
import org.arquillian.pact.consumer.core.client.container.PactConsumerRemoteExtension;
import org.arquillian.pact.consumer.core.client.container.RemoteConsumerPactTest;
import org.arquillian.pact.consumer.core.util.PactConsumerVersionExtractor;
import org.arquillian.pact.consumer.core.util.ResolveClassAnnotation;
import org.arquillian.pact.consumer.spi.Pact;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

public class PactConsumerArchiveAppender implements AuxiliaryArchiveAppender {

    @Inject
    Instance<PactConsumerConfiguration> pactConsumerConfigurationInstance;

    @Override
    public Archive<?> createAuxiliaryArchive() {
        final JavaArchive arquillianPactConsumer = ShrinkWrap.create(JavaArchive.class, "arquillian-pact-consumer.jar")
                // Add Core classes required in container part
                .addClasses(AbstractConsumerPactTest.class,
                        RemoteConsumerPactTest.class, PactConsumerConfiguration.class,
                        MockProviderConfigCreator.class, PactConsumerConfigurator.class,
                        PactConsumerRemoteExtension.class, PactFilesCommand.class, ConsumerProviderPair.class,
                        ResolveClassAnnotation.class)
                .addPackages(true, Pact.class.getPackage())
                .addAsServiceProvider(RemoteLoadableExtension.class, PactConsumerRemoteExtension.class);

        final Properties properties = pactConsumerConfigurationInstance.get().asProperties();
        String configuration = toString(properties);

        arquillianPactConsumer.add(new StringAsset(configuration), "/pact-consumer-configuration.properties");

        final JavaArchive[] pactConsumerDeps = Maven.resolver()
                .resolve("au.com.dius:pact-jvm-consumer_2.11:" + getVersion())
                .withTransitivity().as(JavaArchive.class);

        final JavaArchive merge = merge(arquillianPactConsumer, pactConsumerDeps);
        return merge;
    }

    private String toString(Properties properties) {
        StringWriter stringWriter = new StringWriter();
        properties.list(new PrintWriter(stringWriter));
        return stringWriter.getBuffer().toString();
    }

    private JavaArchive merge(JavaArchive original, JavaArchive[] javaArchives) {
        for (JavaArchive javaArchive : javaArchives) {
            original.merge(javaArchive);
        }

        return original;
    }

    private String getVersion() {
        if (pactConsumerConfigurationInstance.get().isPactArtifactVersionSet()) {
            return pactConsumerConfigurationInstance.get().getPactArtifactVersion();
        } else {
            return PactConsumerVersionExtractor.fromClassPath();
        }
    }
}
