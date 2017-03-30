package org.arquillian.algeron.pact.consumer.core.client.enricher;

import org.arquillian.algeron.consumer.StubServer;
import org.arquillian.algeron.pact.consumer.core.PactConsumerConfiguration;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestEnricher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StubServerEnricher implements TestEnricher {

    private static final Logger logger = Logger.getLogger(StubServerEnricher.class.getName());

    @Inject
    Instance<PactConsumerConfiguration> pactConsumerConfigurationInstance;

    @Override
    public void enrich(Object testCase) {
        final PactConsumerConfiguration pactConsumerConfiguration = pactConsumerConfigurationInstance.get();
        if (pactConsumerConfiguration != null) {
            List<Field> fieldsWithAnnotation = getFieldsWithAnnotation(testCase.getClass(), StubServer.class);
            for (Field stubServer : fieldsWithAnnotation) {

                if (!stubServer.isAccessible()) {
                    stubServer.setAccessible(true);
                }

                if (URL.class.isAssignableFrom(stubServer.getType())) {
                    try {
                        String httpScheme = pactConsumerConfiguration.isHttps() ? "https" : "http";
                        URL url = new URL(httpScheme, pactConsumerConfiguration.getHost(), pactConsumerConfiguration.getPort(), "");
                        stubServer.set(testCase, url);
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException(e);
                    } catch (MalformedURLException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
        }
    }

    @Override
    public Object[] resolve(Method method) {
        Object[] values = new Object[method.getParameterTypes().length];
        final PactConsumerConfiguration pactConsumerConfiguration = pactConsumerConfigurationInstance.get();
        if (pactConsumerConfiguration != null) {
            Integer[] annotatedParameters = annotatedParameters(method);
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (Integer i : annotatedParameters) {
                if (URL.class.isAssignableFrom(parameterTypes[i])) {
                    String httpScheme = pactConsumerConfiguration.isHttps() ? "https" : "http";
                    try {
                        URL url = new URL(httpScheme, pactConsumerConfiguration.getHost(), pactConsumerConfiguration.getPort(), "");
                        values[i] = url;
                    } catch (MalformedURLException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
        }
        return values;
    }

    private Integer[] annotatedParameters(Method method) {
        List<Integer> parametersWithAnnotations = new ArrayList<>();
        final Annotation[][] paramAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < paramAnnotations.length; i++) {
            for (Annotation a : paramAnnotations[i]) {
                if (a instanceof StubServer) {
                    parametersWithAnnotations.add(i);
                }
            }
        }
        return parametersWithAnnotations.toArray(new Integer[parametersWithAnnotations.size()]);
    }

    private static List<Field> getFieldsWithAnnotation(final Class<?> source,
                                                       final Class<? extends Annotation> annotationClass) {
        List<Field> declaredAccessableFields = AccessController
                .doPrivileged(new PrivilegedAction<List<Field>>() {
                    public List<Field> run() {
                        List<Field> foundFields = new ArrayList<Field>();
                        Class<?> nextSource = source;
                        while (nextSource != Object.class) {
                            for (Field field : nextSource.getDeclaredFields()) {
                                if (field.isAnnotationPresent(annotationClass)) {
                                    if (!field.isAccessible()) {
                                        field.setAccessible(true);
                                    }
                                    foundFields.add(field);
                                }
                            }
                            nextSource = nextSource.getSuperclass();
                        }
                        return foundFields;
                    }
                });
        return declaredAccessableFields;
    }
}
