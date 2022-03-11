package org.arquillian.algeron.pact.consumer.core.client;

import au.com.dius.pact.consumer.ConsumerPactRunnerKt;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import org.arquillian.algeron.consumer.StubServer;
import org.arquillian.algeron.pact.consumer.core.AbstractConsumerPactTest;
import org.arquillian.algeron.pact.consumer.core.PactConsumerConfiguration;
import org.arquillian.algeron.pact.consumer.core.PactFilesCommand;
import org.arquillian.algeron.pact.consumer.core.PactMismatchesException;
import org.arquillian.algeron.pact.consumer.core.client.container.ConsumerProviderPair;
import org.arquillian.algeron.pact.consumer.core.client.container.PactConsumerConfigurator;
import org.arquillian.algeron.pact.consumer.core.client.container.PactConsumerRemoteExtension;
import org.arquillian.algeron.pact.consumer.core.client.container.RemoteConsumerPactTest;
import org.arquillian.algeron.pact.consumer.core.client.enricher.StubServerEnricher;
import org.arquillian.algeron.pact.consumer.core.util.PactConsumerVersionExtractor;
import org.arquillian.algeron.pact.consumer.core.util.ResolveClassAnnotation;
import org.arquillian.algeron.pact.consumer.spi.Pact;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

public class PactConsumerArchiveAppender implements AuxiliaryArchiveAppender {

    @Inject
    Instance<PactConsumerConfiguration> pactConsumerConfigurationInstance;

    @Override
    public Archive<?> createAuxiliaryArchive() {
        JavaArchive arquillianPactConsumer = null;
        arquillianPactConsumer = ShrinkWrap.create(JavaArchive.class, "arquillian-pact-consumer.jar")
            // Add Core classes required in container part
            .addClasses(AbstractConsumerPactTest.class,
                RemoteConsumerPactTest.class, PactConsumerConfiguration.class,
                MockProviderConfigCreator.class, PactConsumerConfigurator.class,
                PactConsumerRemoteExtension.class, PactFilesCommand.class, ConsumerProviderPair.class,
                PactMismatchesException.class, ConsumerPactRunnerKt.class, HttpHandler.class, HttpServer.class,
                HttpServerProvider.class,
                ResolveClassAnnotation.class, StubServer.class, StubServerEnricher.class,
                HttpsServer.class, HttpContext.class)
            .addPackages(true, Pact.class.getPackage())
            .addAsServiceProvider(RemoteLoadableExtension.class, PactConsumerRemoteExtension.class);

        arquillianPactConsumer = addSunHttpServer(arquillianPactConsumer);

        final Properties properties = pactConsumerConfigurationInstance.get().asProperties();
        String configuration = toString(properties);

        arquillianPactConsumer.add(new StringAsset(configuration), "/pact-consumer-configuration.properties");

        final JavaArchive[] pactConsumerDeps = Maven.resolver()
            .resolve("au.com.dius:pact-jvm-consumer_2.12:" + getVersion())
            .withTransitivity().as(JavaArchive.class);

        final JavaArchive merge = merge(arquillianPactConsumer, pactConsumerDeps);
        return merge;
    }

    private JavaArchive addSunHttpServer(JavaArchive arquillianPactConsumer) {
        try {
            arquillianPactConsumer.addClass(Class.forName("com.sun.net.httpserver.spi.HttpServerProvider$1"))
                .addClass(Class.forName("sun.net.httpserver.DefaultHttpServerProvider"))
                .addClass(Class.forName("sun.net.httpserver.HttpsServerImpl"))
                .addClass(Class.forName("sun.net.httpserver.HttpServerImpl"))
                .addClass(Class.forName("sun.net.httpserver.HttpContextImpl"))
                .addClass(Class.forName("sun.net.httpserver.ServerImpl"))
                .addClass(Class.forName("sun.net.httpserver.TimeSource"))
                .addClass(Class.forName("sun.net.httpserver.ServerImpl$ServerTimerTask"))
                .addClass(Class.forName("sun.net.httpserver.ServerImpl$ServerTimerTask1"))
                .addClass(Class.forName("sun.net.httpserver.ServerImpl$1"))
                .addClass(Class.forName("sun.net.httpserver.ServerImpl$2"))
                .addClass(Class.forName("sun.net.httpserver.ServerConfig"))
                .addClass(Class.forName("sun.net.httpserver.ServerConfig$1"))
                .addClass(Class.forName("sun.net.httpserver.ServerConfig$2"))
                .addClass(Class.forName("sun.net.httpserver.ContextList"))
                .addClass(Class.forName("sun.net.httpserver.ServerImpl$Dispatcher"))
                .addClass(Class.forName("sun.net.httpserver.HttpError"))
                .addClass(Class.forName("sun.net.httpserver.AuthFilter"))
                .addClass(Class.forName("com.sun.net.httpserver.Filter"))
                .addClass(Class.forName("sun.net.httpserver.ServerImpl$DefaultExecutor"))
                .addClass(Class.forName("sun.net.httpserver.HttpConnection"))
                .addClass(Class.forName("sun.net.httpserver.HttpConnection$State"))
                .addClass(Class.forName("sun.net.httpserver.ServerImpl$Exchange"))
                .addClass(Class.forName("sun.net.httpserver.SSLStreams$InputStream"))
                .addClass(Class.forName("sun.net.httpserver.SSLStreams$OutputStream"))
                .addClass(Class.forName("sun.net.httpserver.Request$ReadStream"))
                .addClass(Class.forName("sun.net.httpserver.Request$WriteStream"))
                .addClass(Class.forName("com.sun.net.httpserver.HttpExchange"))
                .addClass(Class.forName("sun.net.httpserver.HttpsExchangeImpl"))
                .addClass(Class.forName("com.sun.net.httpserver.HttpsExchange"))
                .addClass(Class.forName("sun.net.httpserver.HttpExchangeImpl"))
                .addClass(Class.forName("sun.net.httpserver.Request"))
                .addClass(Class.forName("com.sun.net.httpserver.Headers"))
                .addClass(Class.forName("sun.net.httpserver.ExchangeImpl"))
                .addClass(Class.forName("sun.net.httpserver.ExchangeImpl$1"))
                .addClass(Class.forName("sun.net.httpserver.UnmodifiableHeaders"))
                .addClass(Class.forName("sun.net.httpserver.UndefLengthOutputStream"))
                .addClass(Class.forName("sun.net.httpserver.LeftOverInputStream"))
                .addClass(Class.forName("sun.net.httpserver.ChunkedOutputStream"))
                .addClass(Class.forName("sun.net.httpserver.FixedLengthOutputStream"))
                .addClass(Class.forName("sun.net.httpserver.Event"))
                .addClass(Class.forName("sun.net.httpserver.WriteFinishedEvent"))
                .addClass(Class.forName("sun.net.httpserver.PlaceholderOutputStream"))
                .addClass(Class.forName("sun.net.httpserver.ChunkedInputStream"))
                .addClass(Class.forName("sun.net.httpserver.FixedLengthInputStream"))
                .addClass(Class.forName("com.sun.net.httpserver.Filter$Chain"))
                .addClass(Class.forName("sun.net.httpserver.ServerImpl$Exchange$LinkHandler"))
                .addClass(Class.forName("sun.net.httpserver.Code"))
                .addClass(Class.forName("sun.net.httpserver.StreamClosedException"));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        return arquillianPactConsumer;
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
