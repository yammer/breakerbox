package com.yammer.breakerbox.service.turbine.client;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.netflix.turbine.discovery.Instance;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.core.CircuitBreaker;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

public interface TurbineTenacityClient {
    Optional<ImmutableList<String>> getTenacityPropertyKeys(Instance instance);

    Optional<TenacityConfiguration> getTenacityConfiguration(Instance instance, TenacityPropertyKey key);

    Optional<ImmutableList<CircuitBreaker>> getCircuitBreakers(Instance instance);

    Optional<CircuitBreaker> getCircuitBreaker(Instance instance, TenacityPropertyKey key);

    Optional<CircuitBreaker> modifyCircuitBreaker(Instance instance,
                                                  TenacityPropertyKey key,
                                                  CircuitBreaker.State state);
}
