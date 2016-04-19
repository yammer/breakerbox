package com.yammer.breakerbox.service.turbine;

import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;

import java.util.Collection;

public class YamlInstanceDiscovery implements InstanceDiscovery {
    private final Collection<Instance> instances;

    public YamlInstanceDiscovery(Collection<Instance> instances) {
        this.instances = instances;
    }

    @Override
    public Collection<Instance> getInstanceList() throws Exception {
        return instances;
    }
}
