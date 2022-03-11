package org.arquillian.algeron.pact.provider.spi;

import java.util.Map;

public interface ProviderContextAwareTarget {
    void setStateParams(Map<String, ?> params);
}
