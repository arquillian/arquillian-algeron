package org.arquillian.algeron.pact.provider.core.httptarget;

import au.com.dius.pact.model.RequestResponseInteraction;
import au.com.dius.pact.provider.ConsumerInfo;
import au.com.dius.pact.provider.HttpClientFactory;
import au.com.dius.pact.provider.ProviderClient;
import au.com.dius.pact.provider.ProviderInfo;
import au.com.dius.pact.provider.ProviderVerifier;
import au.com.dius.pact.provider.reporters.ReporterManager;
import au.com.dius.pact.provider.reporters.VerifierReporter;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequest;
import org.arquillian.algeron.configuration.SystemPropertyResolver;
import org.arquillian.algeron.pact.provider.core.recorder.ArquillianVerifierReporter;
import org.arquillian.algeron.pact.provider.spi.ArquillianTestClassAwareTarget;
import org.arquillian.algeron.pact.provider.spi.PactProviderExecutionAwareTarget;
import org.arquillian.algeron.pact.provider.spi.Provider;
import org.arquillian.algeron.pact.provider.spi.ProviderContextAwareTarget;
import org.arquillian.algeron.pact.provider.spi.Target;
import org.arquillian.algeron.pact.provider.spi.TargetRequestFilter;
import org.arquillian.algeron.pact.provider.spi.VerificationReports;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.test.spi.TestClass;

public class HttpTarget implements Target, ArquillianTestClassAwareTarget, PactProviderExecutionAwareTarget,
    ProviderContextAwareTarget {

    private String path;
    private String host;
    private int port;
    private String protocol;
    private boolean insecure;

    private final SystemPropertyResolver systemPropertyResolver = new SystemPropertyResolver();

    private TestClass testClass;
    private Object testInstance;

    private au.com.dius.pact.model.Consumer currentConsumer;
    private RequestResponseInteraction currentRequestResponseInteraction;
    private Injector injector;
    private Map<String, ?> currentStateParams;

    /**
     * @param host
     *     host of tested service
     * @param port
     *     port of tested service
     */
    public HttpTarget(final String host, final int port) {
        this("http", host, port);
    }

    /**
     * Host of tested service is assumed as "localhost"
     *
     * @param port
     *     port of tested service
     */
    public HttpTarget(final int port) {
        this("http", "localhost", port);
    }

    /**
     * @param host
     *     host of tested service
     * @param port
     *     port of tested service
     * @param protocol
     *     protocol of tested service
     */
    public HttpTarget(final String protocol, final String host, final int port) {
        this(protocol, host, port, "/");
    }

    /**
     * @param host
     *     host of tested service
     * @param port
     *     port of tested service
     * @param protocol
     *     protocol of tested service
     * @param path
     *     protocol of the tested service
     */
    public HttpTarget(final String protocol, final String host, final int port, final String path) {
        this(protocol, host, port, path, false);
    }

    /**
     * @param host
     *     host of tested service
     * @param port
     *     port of tested service
     * @param protocol
     *     protocol of the tested service
     * @param path
     *     path of the tested service
     * @param insecure
     *     true if certificates should be ignored
     */
    public HttpTarget(final String protocol, final String host, final int port, final String path,
        final boolean insecure) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.path = removeFinalSlash(path);
        this.insecure = insecure;
    }

    // Due a bug in Pact we need to remove final slash character on path
    private String removeFinalSlash(String path) {
        if (path != null && !path.isEmpty() && path.endsWith("/")) {
            return path.substring(0, path.length() -1);
        }

        return path;
    }

    /**
     * @param url
     *     url of the tested service
     */
    public HttpTarget(final URL url) {
        this(url, false);
    }

    /**
     * @param url
     *     url of the tested service
     * @param insecure
     *     true if certificates should be ignored
     */
    public HttpTarget(final URL url, final boolean insecure) {
        this(url.getProtocol() == null ? "http" : url.getProtocol(),
            url.getHost(),
            url.getPort() == -1 ? 8080 : url.getPort(),
            url.getPath() == null ? "/" : url.getPath(),
            insecure);
    }

    @Override
    public void testInteraction() {
        if (this.currentConsumer == null || this.currentRequestResponseInteraction == null) {
            throw new IllegalArgumentException(
                "Current Consumer or Current Request Response Interaction has not been set.");
        }

        try {
            testInteraction(this.currentConsumer.getName(), this.currentRequestResponseInteraction);
        } finally {
            // Each run should provide a new pair of objects.
            resetCurrentFields();
        }
    }

    @Override
    public void testInteraction(URL url) {
        if (this.currentConsumer == null || this.currentRequestResponseInteraction == null) {
            throw new IllegalArgumentException(
                "Current Consumer or Current Request Response Interaction has not been set.");
        }

        try {
            testInteraction(url, this.currentConsumer.getName(), this.currentRequestResponseInteraction);
        } finally {
            // Each run should provide a new pair of objects.
            resetCurrentFields();
        }
    }

    private void resetCurrentFields() {
        this.currentConsumer = null;
        this.currentRequestResponseInteraction = null;
        this.currentStateParams = null;
    }

    @Override
    public void testInteraction(URL url, String consumer, RequestResponseInteraction interaction) {
        this.protocol = url.getProtocol() == null ? "http" : url.getProtocol();
        this.host = url.getHost();
        this.port = url.getPort() == -1 ? 8080 : url.getPort();
        this.path = removeFinalSlash(url.getPath() == null ? "/" : url.getPath());

        this.testInteraction(consumer, interaction);
    }

    @Override
    public void testInteraction(String consumerName, RequestResponseInteraction interaction) {
        ProviderInfo provider = getProviderInfo();
        ConsumerInfo consumer = new ConsumerInfo(consumerName);
        ProviderVerifier verifier = setupVerifier(interaction, provider, consumer);

        Map<String, Object> failures = new HashMap<>();
        ProviderClient client = new ProviderClient(provider, new HttpClientFactory());
        verifier.verifyResponseFromProvider(provider, interaction, interaction.getDescription(), failures, client,
            currentStateParams == null ? Collections.emptyMap() : currentStateParams);

        try {
            if (!failures.isEmpty()) {
                verifier.displayFailures(failures);
                throw getAssertionError(failures);
            }
        } finally {
            verifier.finaliseReports();
        }
    }

    private ProviderVerifier setupVerifier(RequestResponseInteraction interaction, ProviderInfo provider,
        ConsumerInfo consumer) {
        ProviderVerifier verifier = new ProviderVerifier();

        setupReporters(verifier, provider.getName(), interaction.getDescription());

        verifier.initialiseReporters(provider);
        verifier.reportVerificationForConsumer(consumer, provider);

        verifier.reportStateForInteraction(interaction.displayState(), provider, consumer, true);

        verifier.reportInteractionDescription(interaction);

        return verifier;
    }

    private void setupReporters(ProviderVerifier verifier, String name, String description) {
        String reportDirectory = "target/pact/reports";
        String[] reports = new String[] {};
        boolean reportingEnabled = false;

        VerificationReports verificationReports = testClass.getAnnotation(VerificationReports.class);
        if (verificationReports != null) {
            reportingEnabled = true;
            reportDirectory = verificationReports.reportDir();
            reports = verificationReports.value();
        } else if (systemPropertyResolver.propertyDefined("pact.verification.reports")) {
            reportingEnabled = true;
            reportDirectory = systemPropertyResolver.resolveValue("pact.verification.reportDir:" + reportDirectory);
            reports = systemPropertyResolver.resolveValue("pact.verification.reports:").split(",");
        }

        if (reportingEnabled) {
            File reportDir = new File(reportDirectory);
            reportDir.mkdirs();
            verifier.setReporters(Arrays.stream(reports)
                .filter(r -> !r.isEmpty())
                .map(r -> {
                    final String reportType = r.trim();
                    if ("recorder".equals(reportType)) {
                        return injector.inject(new ArquillianVerifierReporter());
                    } else {
                        VerifierReporter reporter = ReporterManager.createReporter(reportType);
                        reporter.setReportDir(reportDir);
                        reporter.setReportFile(new File(reportDir, name + " - " + description + reporter.getExt()));
                        return reporter;
                    }
                }).collect(Collectors.toList()));
        }
    }

    private ProviderInfo getProviderInfo() {
        Provider provider = testClass.getAnnotation(Provider.class);
        final ProviderInfo providerInfo = new ProviderInfo(provider.value());
        providerInfo.setPort(port);
        providerInfo.setHost(host);
        providerInfo.setProtocol(protocol);
        providerInfo.setPath(path);
        providerInfo.setInsecure(insecure);

        if (testClass != null && testInstance != null) {
            final Method[] methods = testClass.getMethods(TargetRequestFilter.class);

            if (methods != null && methods.length > 0) {
                providerInfo.setRequestFilter(
                    (Consumer<HttpRequest>) httpRequest -> Arrays.stream(methods).forEach(method -> {
                        try {
                            method.invoke(testInstance, httpRequest);
                        } catch (Throwable t) {
                            throw new AssertionError(
                                "Request filter method " + method.getName() + " failed with an exception", t);
                        }
                    }));
            }
        }

        return providerInfo;
    }

    private AssertionError getAssertionError(final Map<String, Object> mismatches) {
        final Collection<Object> values = mismatches.values();

        StringBuilder error = new StringBuilder(System.lineSeparator());

        int i = 0;
        for (Object value : values) {
            String errPrefix = String.valueOf(i) + ": ";
            error.append(errPrefix);
            if (value instanceof Throwable) {
                error.append(exceptionMessage((Throwable) value, errPrefix.length()));
            } else if (value instanceof Map) {
                error.append(convertMapToErrorString((Map) value));
            } else {
                error.append(value.toString());
            }
            error.append(System.lineSeparator());

            i++;
        }

        return new AssertionError(error.toString());
    }

    private String exceptionMessage(Throwable err, int prefixLength) {
        String message = err.getMessage();
        if (message.contains("\n")) {
            String padString = StringUtils.leftPad("", prefixLength);

            final String[] split = message.split("\n");

            StringBuilder messageError = new StringBuilder();

            String firstElement = "";
            if (split.length > 0) {
                firstElement = split[0];
            }

            messageError.append(firstElement)
                .append(System.lineSeparator());

            for (int i = 1; i < split.length; i++) {
                messageError.append(padString)
                    .append(split[i])
                    .append(System.lineSeparator());
            }

            return messageError.toString();
        } else {
            return message;
        }
    }

    private String convertMapToErrorString(Map mismatches) {
        if (mismatches.containsKey("comparison")) {
            Object comparison = mismatches.get("comparison");
            if (mismatches.containsKey("diff")) {
                return String.format("%s%n%s", mapToString((Map) comparison),
                    listToString(((List) mismatches.get("diff"))));
            } else {
                if (comparison instanceof Map) {
                    return mapToString((Map) comparison);
                } else {
                    return String.valueOf(comparison);
                }
            }
        } else {
            return mapToString(mismatches);
        }
    }

    private String listToString(List<String> list) {
        return list.stream().collect(Collectors.joining(System.lineSeparator())).toString();
    }

    private String mapToString(Map comparison) {
        return comparison.entrySet().stream()
            .map(e -> String.valueOf(((Map.Entry) e).getKey()) + " -> " + ((Map.Entry) e).getValue())
            .collect(Collectors.joining(System.lineSeparator())).toString();
    }

    @Override
    public void setTestClass(TestClass testClass, Object testInstance) {
        this.testClass = testClass;
        this.testInstance = testInstance;
    }

    @Override
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    @Override
    public void setConsumer(au.com.dius.pact.model.Consumer consumer) {
        this.currentConsumer = consumer;
    }

    @Override
    public void setRequestResponseInteraction(RequestResponseInteraction requestResponseInteraction) {
        this.currentRequestResponseInteraction = requestResponseInteraction;
    }

    @Override
    public void setStateParams(Map<String, ?> params) {
        this.currentStateParams = Collections.singletonMap("providerState", params);
    }
}
