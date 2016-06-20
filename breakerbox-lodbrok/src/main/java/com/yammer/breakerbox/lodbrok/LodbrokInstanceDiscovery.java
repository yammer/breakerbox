package com.yammer.breakerbox.lodbrok;

import com.google.common.collect.ImmutableList;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;
import com.yammer.breakerbox.turbine.TurbineInstanceDiscovery;
import com.yammer.lodbrok.discovery.core.Task;
import com.yammer.lodbrok.discovery.core.store.LodbrokInstanceStore;

import java.util.Collection;
import java.util.stream.Collectors;

public class LodbrokInstanceDiscovery implements InstanceDiscovery {
    private final Collection<LodbrokInstanceStore> lodbrokInstanceStores;
    public static final String LODBROK_GLOBAL = "lodbrok-global";
    public static final String LODBROK_ROUTE_IP = "lodbrok-route-ip";
    public static final String LODBROK_ROUTE_ID = "lodbrok-route-id";
    public static final String LODBROK_REGION = "lodbrok-region";
    public static final String DEFAULT_TENACITY_METRICS_STREAM = "/tenacity/metrics.stream";

    public LodbrokInstanceDiscovery(Collection<LodbrokInstanceStore> lodbrokInstanceStores) {
        this.lodbrokInstanceStores = lodbrokInstanceStores;
    }

    public LodbrokInstanceDiscovery(LodbrokInstanceStore lodbrokInstanceStore) {
        this(ImmutableList.of(lodbrokInstanceStore));
    }

    @Override
    public Collection<Instance> getInstanceList() throws Exception {
        final Collection<Instance> instances = lodbrokInstanceStores
            .stream()
            .flatMap((lodbrokInstanceStore) ->
                 lodbrokInstanceStore
                    .instances()
                    .values()
                    .stream()
                    .flatMap((lodbrokInstance) -> lodbrokInstance
                        .getTasks()
                        .values()
                        .stream())
                    .map((task) -> {
                        final Instance instance = new Instance(
                                String.format("%s:%s", task.getId(), Integer.parseInt(task.getPortsList().get(0))),
                                task.getName(), true);
                        setLodbrokRouteAttributes(lodbrokInstanceStore, instance, task);
                        return instance;
                    }))
            .collect(Collectors.toList());
        registerDynamicClusterNames(instances);
        return instances;
    }

    public static void setLodbrokRouteAttributes(LodbrokInstanceStore lodbrokInstanceStore, Instance instance, Task task) {
        instance.getAttributes().put(LODBROK_ROUTE_IP, task.getIp().getHostAddress());
        instance.getAttributes().put(LODBROK_ROUTE_ID, task.getId());
        instance.getAttributes().put(TurbineInstanceDiscovery.BREAKERBOX_INSTANCE_ID,
                String.format("%s: %s", lodbrokInstanceStore.getName(), task.getId()));
        instance.getAttributes().put(LODBROK_GLOBAL, lodbrokInstanceStore.getLodbrokUri().toString());
        instance.getAttributes().put(LODBROK_REGION, lodbrokInstanceStore.getName());
    }

    private void registerDynamicClusterNames(Collection<Instance> instances) {
        final Collection<String> clusterNames = instances
                .stream()
                .map(Instance::getCluster)
                .collect(Collectors.toSet());
        TurbineInstanceDiscovery.registerClusters(clusterNames, DEFAULT_TENACITY_METRICS_STREAM);
    }
}