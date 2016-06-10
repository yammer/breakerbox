package com.yammer.breakerbox.service.turbine.client;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.netflix.turbine.discovery.Instance;
import com.yammer.tenacity.client.TenacityClient;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.core.CircuitBreaker;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import java.net.URI;

public class DelegatingTenacityClient implements TurbineTenacityClient {
    private final TenacityClient client;

    public DelegatingTenacityClient(TenacityClient client) {
        this.client = client;
    }

    @Override
    public Optional<ImmutableList<String>> getTenacityPropertyKeys(Instance instance) {
        return client.getTenacityPropertyKeys(toUri(instance));
    }

    @Override
    public Optional<TenacityConfiguration> getTenacityConfiguration(Instance instance, TenacityPropertyKey key) {
        return client.getTenacityConfiguration(toUri(instance), key);
    }

    @Override
    public Optional<ImmutableList<CircuitBreaker>> getCircuitBreakers(Instance instance) {
        return client.getCircuitBreakers(toUri(instance));
    }

    @Override
    public Optional<CircuitBreaker> getCircuitBreaker(Instance instance, TenacityPropertyKey key) {
        return client.getCircuitBreaker(toUri(instance), key);
    }

    @Override
    public Optional<CircuitBreaker> modifyCircuitBreaker(Instance instance, TenacityPropertyKey key, CircuitBreaker.State state) {
        return client.modifyCircuitBreaker(toUri(instance), key, state);
    }

    private static URI toUri(Instance instance) {
        final String rawHostname = instance.getHostname().trim();
        if (rawHostname.startsWith("http")) {
            return URI.create(rawHostname);
        } else {
            return URI.create("http://" + rawHostname);
        }
    }
}
