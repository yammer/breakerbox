package com.yammer.breakerbox.turbine;

import com.google.common.collect.ImmutableList;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConcatenatingInstanceDiscovery implements InstanceDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcatenatingInstanceDiscovery.class);
    private final Collection<InstanceDiscovery> instanceDiscoveries;

    public ConcatenatingInstanceDiscovery(Collection<InstanceDiscovery> instanceDiscoveries) {
        this.instanceDiscoveries = instanceDiscoveries;
    }

    public ConcatenatingInstanceDiscovery(InstanceDiscovery... instanceDiscoveries) {
        this(ImmutableList.copyOf(instanceDiscoveries));
    }

    @Override
    public Collection<Instance> getInstanceList() throws Exception {
        return instanceDiscoveries
                .stream()
                .flatMap((instanceDiscovery) -> {
                    try {
                        return instanceDiscovery.getInstanceList().stream();
                    } catch (Exception err) {
                        LOGGER.warn("Discovering instances with {}", instanceDiscovery, err);
                    }
                    return Stream.empty();
                })
                .collect(Collectors.toList());
    }
}
