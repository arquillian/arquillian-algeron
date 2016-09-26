package org.arquillian.pact.provider.spi;

import au.com.dius.pact.model.*;

/**
 * Interface to make Target aware of Pact Provider execution elements.
 */
public interface PactProviderExecutionAwareTarget {

    void setConsumer(au.com.dius.pact.model.Consumer consumer);
    void setRequestResponseInteraction(RequestResponseInteraction requestResponseInteraction);

}
