package com.yammer.breakerbox.turbine.client;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.turbine.discovery.Instance;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.core.CircuitBreaker;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import java.net.URI;

public interface TurbineTenacityClient {
    Optional<ImmutableList<String>> getTenacityPropertyKeys(Instance instance);

    Optional<TenacityConfiguration> getTenacityConfiguration(Instance instance, TenacityPropertyKey key);

    Optional<ImmutableList<CircuitBreaker>> getCircuitBreakers(Instance instance);

    Optional<CircuitBreaker> getCircuitBreaker(Instance instance, TenacityPropertyKey key);

    Optional<CircuitBreaker> modifyCircuitBreaker(Instance instance,
                                                  TenacityPropertyKey key,
                                                  CircuitBreaker.State state);

    static URI toUri(Instance instance) {
        final String rawHostname = instance.getHostname().trim();
        if (rawHostname.startsWith("http")) {
            return URI.create(rawHostname);
        } else {
            final String protocolKey = "turbine.protocol." + instance.getCluster();
            final DynamicStringProperty protocolConfig = DynamicPropertyFactory
                    .getInstance()
                    .getStringProperty(protocolKey, "https");

            return URI.create(protocolConfig.get() + "://" + rawHostname);
        }
    }
}
