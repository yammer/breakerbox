package com.yammer.breakerbox.service.turbine;

import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;
import com.yammer.lodbrok.discovery.core.LodbrokInstance;
import com.yammer.lodbrok.discovery.core.Task;
import com.yammer.lodbrok.discovery.core.store.LodbrokInstanceStore;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LodbrokInstanceDiscovery implements InstanceDiscovery {
    private final LodbrokInstanceStore lodbrokInstanceStore;
    private final URI lodbrokGlobalUri;
    public static final String LODBROK_ROUTE = "lodbrok-route";
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
                        final Instance instance = new Instance(UriBuilder
                                .fromUri(lodbrokGlobalUri)
                                .port(Integer.parseInt(task.getPortsList().get(0))).build().toString(),
                                task.getName(), true);
                        setLodbrokRouteAttribute(instance, task);
                        return instance;
                    })
                    .collect(Collectors.toList());
    }

    public static void setLodbrokRouteAttribute(Instance instance, Task task) {
        instance.getAttributes().putIfAbsent(LODBROK_ROUTE, String.format("%s-%s", task.getIp().getHostAddress(), task.getId()));
    }

    private void registerDynamicClusterNames(Collection<LodbrokInstance> instances) {
        final Collection<String> clusterNames = instances.stream()
                .flatMap(TO_TASKS_STREAM)
                .map(Task::getName)
                .collect(Collectors.toList());
        TurbineInstanceDiscovery.registerClusters(clusterNames, DEFAULT_TENACITY_METRICS_STREAM);
    }
}