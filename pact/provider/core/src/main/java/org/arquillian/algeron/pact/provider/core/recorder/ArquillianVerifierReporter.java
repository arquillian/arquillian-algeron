package org.arquillian.algeron.pact.provider.core.recorder;

import au.com.dius.pact.model.Interaction;
import au.com.dius.pact.model.Pact;
import au.com.dius.pact.model.PactSource;
import au.com.dius.pact.model.UrlPactSource;
import au.com.dius.pact.provider.IConsumerInfo;
import au.com.dius.pact.provider.IProviderInfo;
import au.com.dius.pact.provider.reporters.VerifierReporter;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.arquillian.recorder.reporter.PropertyEntry;
import org.arquillian.recorder.reporter.event.PropertyReportEvent;
import org.arquillian.recorder.reporter.model.entry.GroupEntry;
import org.arquillian.recorder.reporter.model.entry.TextEntry;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.annotation.Inject;

public class ArquillianVerifierReporter implements VerifierReporter {

    @Inject
    Event<PropertyReportEvent> propertyReportEvent;

    private GroupEntry verificationGroup = new GroupEntry();
    private GroupEntry interactionGroup;
    private GroupEntry responseGroup;
    private GroupEntry headersGroup;
    private GroupEntry metadataGroup;
    private GroupEntry stateGroup;

    @Override
    public void finaliseReport() {
        propertyReportEvent.fire(new PropertyReportEvent(verificationGroup));
    }

    @Override
    public void reportVerificationForConsumer(IConsumerInfo consumer, IProviderInfo provider) {
        verificationGroup.setName(
            String.format("Verifying a pact between %s and %s", consumer.getName(), provider.getName()));
    }

    @Override
    public void verifyConsumerFromUrl(UrlPactSource urlPactSource, IConsumerInfo consumerInfo) {

    }

    @Override
    public void verifyConsumerFromFile(PactSource pactSource, IConsumerInfo consumerInfo) {

    }

    @Override
    public void warnProviderHasNoConsumers(IProviderInfo providerInfo) {
        appendTextEntry(
            String.format("WARNING: There are no consumers to verify for provider %s", providerInfo.getName()),
            this.verificationGroup);
    }

    @Override
    public void warnPactFileHasNoInteractions(Pact<Interaction> pact) {
        appendTextEntry("WARNING: Pact file has no interactions", this.verificationGroup);
    }

    @Override
    public void interactionDescription(Interaction interaction) {
        this.interactionGroup = new GroupEntry(interaction.getDescription());
        this.verificationGroup.getPropertyEntries().add(this.interactionGroup);
    }

    @Override
    public void stateForInteraction(String state, IProviderInfo provider, IConsumerInfo consumer, boolean isSetup) {
        this.stateGroup = new GroupEntry("Given State");
        appendTextEntry(state, this.stateGroup);
        this.verificationGroup.getPropertyEntries().add(this.stateGroup);
    }

    @Override
    public void warnStateChangeIgnored(String state, IProviderInfo providerInfo, IConsumerInfo consumerInfo) {
        appendTextEntry("WARNING: State Change ignored as there is no stateChange URL", this.stateGroup);
    }

    @Override
    public void stateChangeRequestFailedWithException(String state, IProviderInfo providerInfo, IConsumerInfo consumerInfo,
                                                      boolean isSetup, Exception e, boolean printStackTrace) {
        appendTextEntry(String.format("State %s Change Request Failed - %s", state, e.getMessage()), this.stateGroup);
    }

    @Override
    public void stateChangeRequestFailed(String state, IProviderInfo providerInfo, boolean isSetup, String httpStatus) {
        appendTextEntry(String.format("State %s Change Request Failed - %s", state, httpStatus), this.stateGroup);
    }

    @Override
    public void warnStateChangeIgnoredDueToInvalidUrl(String state, IProviderInfo providerInfo, boolean isSetup,
                                                      Object stateChangeHandler) {
        appendTextEntry(String.format("WARNING: State Change ignored as there is no stateChange URL, received %s",
            stateChangeHandler.toString()), this.stateGroup);
    }

    @Override
    public void requestFailed(IProviderInfo providerInfo, Interaction interaction, String interactionMessage, Exception e,
                              boolean printStackTrace) {
        appendTextEntry(String.format("Request Failed on %s - %s", interactionMessage, e.getMessage()), this.interactionGroup);
    }

    @Override
    public void returnsAResponseWhich() {
        this.responseGroup = new GroupEntry("With Response");
        this.interactionGroup.getPropertyEntries().add(this.responseGroup);
    }

    @Override
    public void statusComparisonOk(int status) {
        appendTextEntry(String.format("has status code %s (OK)", status), this.responseGroup);
    }

    @Override
    public void statusComparisonFailed(int status, Object comparison) {
        appendTextEntry(String.format("has status code %s (FAILED)", status), this.responseGroup);
    }

    @Override
    public void includesHeaders() {
        this.headersGroup = new GroupEntry("include headers");
        this.responseGroup.getPropertyEntries().add(this.headersGroup);
    }

    @Override
    public void headerComparisonOk(String key, List<String> values) {
        values.forEach(value -> appendTextEntry(String.format("%s with value %s (OK)", key, value), this.headersGroup));
    }

    @Override
    public void headerComparisonFailed(String key, List<String> values, Object comparison) {
        values.forEach(value -> appendTextEntry(String.format("%s with value %s (FAILED)", key, value), this.headersGroup));
    }

    @Override
    public void bodyComparisonOk() {
        appendTextEntry("has a matching body (OK)", this.responseGroup);
    }

    @Override
    public void bodyComparisonFailed(Object comparison) {
        appendTextEntry("has a matching body (FAILED)", this.responseGroup);
    }

    @Override
    public void verificationFailed(Interaction interaction, Exception e, boolean printStackTrace) {
        appendTextEntry(String.format("Verification Failed - %s", e.getMessage()), this.responseGroup);
    }

    @Override
    public void generatesAMessageWhich() {
        appendTextEntry("generates a message which", this.interactionGroup);
    }

    @Override
    public void displayFailures(Map failures) {
        GroupEntry failuresGroup = new GroupEntry("Failures");

        final Set<Map.Entry<?, ?>> errors = failures.entrySet();

        for (Map.Entry<?, ?> error : errors) {
            final GroupEntry failureGroup = new GroupEntry(error.getKey().toString());
            failureGroup.getPropertyEntries().add(getErrorMessage(error.getValue()));
            failuresGroup.getPropertyEntries().add(failureGroup);
        }

        this.verificationGroup.getPropertyEntries().add(failuresGroup);
    }

    private void appendTextEntry(String text, GroupEntry groupEntry) {
        TextEntry textEntry = new TextEntry(text);
        groupEntry.getPropertyEntries().add(textEntry);
    }

    private PropertyEntry getErrorMessage(Object error) {
        if (error instanceof Throwable) {
            return new TextEntry(((Throwable) error).getMessage());
        } else if (isMapWithDiff(error)) {
            return serializeDiff((Map) error);
        } else if (error instanceof String) {
            return new TextEntry((String) error);
        } else if (error instanceof Map) {
            return serializeMap((Map) error);
        } else {
            return new TextEntry(error.toString());
        }
    }

    private boolean isMapWithDiff(Object error) {
        return error instanceof Map && ((Map) error).containsKey("diff");
    }

    private PropertyEntry serializeMap(Map map) {
        final GroupEntry groupEntry = new GroupEntry();
        final Set<Map.Entry<?, ?>> params = map.entrySet();

        for (Map.Entry<?, ?> param : params) {
            final TextEntry entry = new TextEntry(String.format("( %s : %s )", param.getKey(), param.getValue()));
            groupEntry.getPropertyEntries().add(entry);
        }

        return groupEntry;
    }

    private GroupEntry serializeDiff(Map errorWithDiff) {

        GroupEntry diff = new GroupEntry("Diff");

        final Map comparison = (Map) errorWithDiff.get("comparison");
        addDiffs(diff, (List<?>) errorWithDiff.get("diff"));
        diff.getPropertyEntries().add(serializeMap(comparison));

        return diff;
    }

    private void addDiffs(final GroupEntry diffGroupEntry, final List<?> diffs) {
        diffs.stream()
            .map(diff -> new TextEntry(diff.toString()))
            .forEach(diff -> diffGroupEntry.getPropertyEntries().add(diff));
    }

    @Override
    public void setReportDir(File reportDir) {
        // This is not valid since Arquillian Recorder Reporter is who writes the report
    }

    @Override
    public void setReportFile(File reportFile) {
        // This is not valid since Arquillian Recorder Reporter is who writes the report
    }

    @Override
    public void initialise(IProviderInfo provider) {

    }

    @Override
    public void pactLoadFailureForConsumer(IConsumerInfo consumerInfo, String message) {

    }

    @Override
    public void errorHasNoAnnotatedMethodsFoundForInteraction(Interaction interaction) {

    }

    @Override
    public String getExt() {
        return null;
    }

    @Override
    public void includesMetadata() {
        this.metadataGroup = new GroupEntry("include metadata");
        this.responseGroup.getPropertyEntries().add(this.metadataGroup);
    }

    @Override
    public void metadataComparisonFailed(String key, Object value, Object comparision) {
        appendTextEntry(String.format("%s with value %s (FAILED)", key, value), this.metadataGroup);
    }

    @Override
    public void metadataComparisonOk() {
        appendTextEntry("has a matching metadata (OK)", this.metadataGroup);

    }

    @Override
    public void metadataComparisonOk(String key, Object value) {
        appendTextEntry(String.format("%s with value %s (OK)", key, value), this.metadataGroup);


    }
}
