package org.arquillian.pact.provider.core;


import static java.util.stream.Collectors.toList;

import au.com.dius.pact.model.Pact;
import org.arquillian.pact.provider.api.Pacts;
import org.arquillian.pact.provider.spi.Consumer;
import org.arquillian.pact.provider.spi.Provider;
import org.arquillian.pact.provider.spi.loader.PactLoader;
import org.arquillian.pact.provider.spi.loader.PactSource;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reads provided Pacts from defined @PactSource or any annotation meta-annotated with @PactSource
 */
public class PactsReader {

    @Inject
    @SuiteScoped
    InstanceProducer<Pacts> pactsInstanceProducer;

    public void readPacts(@Observes BeforeClass test) {
        List<Pact> pacts = getPacts(test);
        pactsInstanceProducer.set(new Pacts(pacts));
    }

    protected List<Pact> getPacts(BeforeClass test) {
        final TestClass testClass = test.getTestClass();

        final Provider providerInfo = testClass.getAnnotation(Provider.class);
        if (providerInfo == null) {
            throw new IllegalArgumentException(String.format("Provider name should be set by using %s",Provider.class.getName()));
        }

        final String serviceName = providerInfo.value();

        final Consumer consumerInfo = testClass.getAnnotation(Consumer.class);
        final String consumerName = consumerInfo != null ? consumerInfo.value() : null;

        List<Pact> pacts = new ArrayList<>();
        try {
            pacts = getPactSource(testClass).load(serviceName).stream()
                    .filter(p -> consumerName == null || p.getConsumer().getName().equals(consumerName))
                    .collect(toList());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return pacts;
    }

    protected PactLoader getPactSource(final TestClass testClass) {

        // Gets a test annotated directly with PactSource annotation
        final PactSource pactSource = testClass.getAnnotation(PactSource.class);

        // Gets annotations that might be meta annotated with PactSource annotation
        final List<Annotation> pactLoaders = Arrays.stream(testClass.getJavaClass().getAnnotations())
                .filter(annotation -> annotation.annotationType().getAnnotation(PactSource.class) != null)
                .collect(toList());

        // It can only be one PactSource in test
        if ((pactSource == null ? 0 : 1) + pactLoaders.size() != 1) {
            throw new IllegalArgumentException(String.format("Exactly one pact source should be set, but %s are set", (pactSource == null ? 0 : 1) + pactLoaders.size()));
        }

        try {
            if (pactSource != null) {
                final Class<? extends PactLoader> pactLoaderClass = pactSource.value();
                try {
                    // Checks if there is a constructor with one argument of type Class.
                    final Constructor<? extends PactLoader> contructorWithClass = pactLoaderClass.getDeclaredConstructor(Class.class);
                    contructorWithClass.setAccessible(true);
                    return contructorWithClass.newInstance(testClass.getJavaClass());
                } catch(NoSuchMethodException e) {
                    return pactLoaderClass.newInstance();
                }
            } else {
                final Annotation annotation = pactLoaders.iterator().next();
                return annotation.annotationType().getAnnotation(PactSource.class).value()
                        .getConstructor(annotation.annotationType()).newInstance(annotation);
            }
        } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException("Error while creating pact source", e);
        }
    }
}
