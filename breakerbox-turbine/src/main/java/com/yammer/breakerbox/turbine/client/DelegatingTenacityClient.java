package com.yammer.breakerbox.turbine.client;

import com.netflix.turbine.discovery.Instance;
import com.yammer.tenacity.client.TenacityClient;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.core.CircuitBreaker;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import java.util.Collection;
import java.util.Optional;

public class DelegatingTenacityClient implements TurbineTenacityClient {
    private final TenacityClient client;

    public DelegatingTenacityClient(TenacityClient client) {
        this.client = client;
    }

    @Override
    public Optional<Collection<String>> getTenacityPropertyKeys(Instance instance) {
        return client.getTenacityPropertyKeys(TurbineTenacityClient.toUri(instance));
    }

    @Override
    public Optional<TenacityConfiguration> getTenacityConfiguration(Instance instance, TenacityPropertyKey key) {
        return client.getTenacityConfiguration(TurbineTenacityClient.toUri(instance), key);
    }

    @Override
    public Optional<Collection<CircuitBreaker>> getCircuitBreakers(Instance instance) {
        return client.getCircuitBreakers(TurbineTenacityClient.toUri(instance));
    }

    @Override
    public Optional<CircuitBreaker> getCircuitBreaker(Instance instance, TenacityPropertyKey key) {
        return client.getCircuitBreaker(TurbineTenacityClient.toUri(instance), key);
    }

    @Override
    public Optional<CircuitBreaker> modifyCircuitBreaker(Instance instance, TenacityPropertyKey key, CircuitBreaker.State state) {
        return client.modifyCircuitBreaker(TurbineTenacityClient.toUri(instance), key, state);
    }
}
