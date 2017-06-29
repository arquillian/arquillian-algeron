package org.arquillian.algeron.pact.consumer.core;

import au.com.dius.pact.consumer.PactVerificationResult;

public class PactMismatchesException extends AssertionError {
    private final PactVerificationResult mismatches;

    public PactMismatchesException(PactVerificationResult mismatches) {
        super(mismatches.getDescription());
        this.mismatches = mismatches;
    }
}
