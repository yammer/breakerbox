package com.yammer.breakerbox.lodbrok.tenacity;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.net.InetAddresses;
import com.netflix.turbine.discovery.Instance;
import com.yammer.breakerbox.lodbrok.LodbrokInstanceDiscovery;
import com.yammer.breakerbox.turbine.client.TurbineTenacityClient;
import com.yammer.lodbrok.discovery.core.Task;
import com.yammer.lodbrok.discovery.core.tenacity.LodbrokTenacityClient;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.core.CircuitBreaker;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;

public class DelegatingLodbrokTenacityClient implements TurbineTenacityClient {
    private final LodbrokTenacityClient client;

    public DelegatingLodbrokTenacityClient(LodbrokTenacityClient client) {
        this.client = client;
    }

    @Override
    public Optional<ImmutableList<String>> getTenacityPropertyKeys(Instance instance) {
        return client.getTenacityPropertyKeys(toUri(instance), toTask(instance));
    }

    @Override
    public Optional<TenacityConfiguration> getTenacityConfiguration(Instance instance, TenacityPropertyKey key) {
        return client.getTenacityConfiguration(toUri(instance), toTask(instance), key);
    }

    @Override
    public Optional<ImmutableList<CircuitBreaker>> getCircuitBreakers(Instance instance) {
        return client.getCircuitBreakers(toUri(instance), toTask(instance));
    }

    @Override
    public Optional<CircuitBreaker> getCircuitBreaker(Instance instance, TenacityPropertyKey key) {
        return client.getCircuitBreaker(toUri(instance), toTask(instance), key);
    }

    @Override
    public Optional<CircuitBreaker> modifyCircuitBreaker(Instance instance, TenacityPropertyKey key, CircuitBreaker.State state) {
        return client.modifyCircuitBreaker(toUri(instance), toTask(instance), key, state);
    }

    private URI toUri(Instance instance) {
        return UriBuilder
                .fromUri(instance.getAttributes().get(LodbrokInstanceDiscovery.LODBROK_GLOBAL))
                .port(Integer.parseInt(instance.getHostname().split(":")[1]))
                .build();
    }

    public static Task toTask(Instance instance) {
        final Map<String, String> attributes = instance.getAttributes();
        return new Task(attributes.get(LodbrokInstanceDiscovery.LODBROK_ROUTE_ID),
                InetAddresses.forString(attributes.get(LodbrokInstanceDiscovery.LODBROK_ROUTE_IP)),
                instance.getCluster(),
                ImmutableList.of(), //ignored
                "Running");
    }
}
