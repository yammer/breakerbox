package com.yammer.breakerbox.turbine;

import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;

import java.util.Collection;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class RegisterClustersInstanceDiscoveryWrapper implements InstanceDiscovery {
    private final InstanceDiscovery delegateInstanceDiscovery;
    private final String urlSuffix;

    public RegisterClustersInstanceDiscoveryWrapper(InstanceDiscovery delegateInstanceDiscovery,
                                                    String urlSuffix) {
        this.delegateInstanceDiscovery = delegateInstanceDiscovery;
        this.urlSuffix = urlSuffix;
    }

    public static RegisterClustersInstanceDiscoveryWrapper wrap(InstanceDiscovery instanceDiscovery) {
        return new RegisterClustersInstanceDiscoveryWrapper(instanceDiscovery, TurbineInstanceDiscovery.DEFAULT_URL_SUFFIX);
    }

    public static RegisterClustersInstanceDiscoveryWrapper wrap(InstanceDiscovery instanceDiscovery,
                                                         String urlSuffix) {
        return new RegisterClustersInstanceDiscoveryWrapper(instanceDiscovery, urlSuffix);
    }

    @Override
    public Collection<Instance> getInstanceList() throws Exception {
        final Collection<Instance> instances = delegateInstanceDiscovery.getInstanceList();
        TurbineInstanceDiscovery.registerClusters(
                instances.stream()
                .map(Instance::getCluster)
                .collect(Collectors.toCollection(TreeSet::new)), urlSuffix);
        return instances;
    }
}
