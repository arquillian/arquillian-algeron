package org.arquillian.pact.consumer.core.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PactConsumerVersionExtractorTest {


    @Test
    public void should_extract_version_of_pact_from_manifest() {
        assertThat(PactConsumerVersionExtractor.fromClassPath()).startsWith("3.5");
    }

}
