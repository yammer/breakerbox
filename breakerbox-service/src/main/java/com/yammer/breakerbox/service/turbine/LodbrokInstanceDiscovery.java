package com.yammer.breakerbox.service.turbine;

import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;
import com.yammer.lodbrok.discovery.core.LodbrokInstance;
import com.yammer.lodbrok.discovery.core.Task;
import com.yammer.lodbrok.discovery.core.store.LodbrokInstanceStore;

import java.net.URI;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LodbrokInstanceDiscovery implements InstanceDiscovery {
    private final LodbrokInstanceStore lodbrokInstanceStore;
    private final URI lodbrokGlobalUri;
    public static final String LODBROK_GLOBAL = "lodbrok-global";
    public static final String LODBROK_ROUTE_IP = "lodbrok-route-ip";
    public static final String LODBROK_ROUTE_ID = "lodbrok-route-id";
    public static final String DEFAULT_TENACITY_METRICS_STREAM = "/tenacity/metrics.stream";
    private static Function<LodbrokInstance, Stream<Task>> TO_TASKS_STREAM =
            (lodbrokInstance) -> lodbrokInstance
                        .getTasks()
                        .values()
                        .stream();

    public LodbrokInstanceDiscovery(LodbrokInstanceStore lodbrokInstanceStore,
                                    URI lodbrokGlobalUri) {
        this.lodbrokInstanceStore = lodbrokInstanceStore;
        this.lodbrokGlobalUri = lodbrokGlobalUri;
    }

    @Override
    public Collection<Instance> getInstanceList() throws Exception {
        final Collection<LodbrokInstance> instances = lodbrokInstanceStore.instances().values();
        registerDynamicClusterNames(instances);
        return instances
                    .stream()
                    .flatMap(TO_TASKS_STREAM)
                    .map((task) -> {
                        final Instance instance = new Instance(
                                String.format("%s:%s", task.getId(),
                                        Integer.parseInt(task.getPortsList().get(0))),
                                task.getName(), true);
                        setLodbrokRouteAttributes(instance, task);
                        return instance;
                    })
                    .collect(Collectors.toList());
    }

    public void setLodbrokRouteAttributes(Instance instance, Task task) {
        instance.getAttributes().put(LODBROK_ROUTE_IP, task.getIp().getHostAddress());
        instance.getAttributes().put(LODBROK_ROUTE_ID, task.getId());
        instance.getAttributes().put(LODBROK_GLOBAL, lodbrokGlobalUri.toString());
    }

    private void registerDynamicClusterNames(Collection<LodbrokInstance> instances) {
        final Collection<String> clusterNames = instances.stream()
                .flatMap(TO_TASKS_STREAM)
                .map(Task::getName)
                .collect(Collectors.toList());
        TurbineInstanceDiscovery.registerClusters(clusterNames, DEFAULT_TENACITY_METRICS_STREAM);
    }
}