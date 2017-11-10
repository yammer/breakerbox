package com.yammer.breakerbox.turbine.client;

import com.netflix.turbine.discovery.Instance;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.core.CircuitBreaker;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

public interface TurbineTenacityClient {
    Optional<Collection<String>> getTenacityPropertyKeys(Instance instance);

    Optional<TenacityConfiguration> getTenacityConfiguration(Instance instance, TenacityPropertyKey key);

    Optional<Collection<CircuitBreaker>> getCircuitBreakers(Instance instance);

    Optional<CircuitBreaker> getCircuitBreaker(Instance instance, TenacityPropertyKey key);

    Optional<CircuitBreaker> modifyCircuitBreaker(Instance instance,
                                                  TenacityPropertyKey key,
                                                  CircuitBreaker.State state);

    static URI toUri(Instance instance) {
        final String rawHostname = instance.getHostname().trim();
        if (rawHostname.startsWith("http")) {
            return URI.create(rawHostname);
        } else {
            return URI.create("http://" + rawHostname);
        }
    }
}
