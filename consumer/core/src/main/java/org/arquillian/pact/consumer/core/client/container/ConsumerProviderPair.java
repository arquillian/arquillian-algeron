package org.arquillian.pact.consumer.core.client.container;

public class ConsumerProviderPair {

    private String consumer;
    private String provider;

    public ConsumerProviderPair(String consumer, String provider) {
        this.consumer = consumer;
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    public String getConsumer() {
        return consumer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConsumerProviderPair that = (ConsumerProviderPair) o;

        if (consumer != null ? !consumer.equals(that.consumer) : that.consumer != null) return false;
        return provider != null ? provider.equals(that.provider) : that.provider == null;

    }

    @Override
    public int hashCode() {
        int result = consumer != null ? consumer.hashCode() : 0;
        result = 31 * result + (provider != null ? provider.hashCode() : 0);
        return result;
    }
}
