package com.yammer.breakerbox.service.turbine;


import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Discovers instances in a Kubernetes cluster. The idea is to
 * find ReplicationControllers or ReplicaSets, check if their instances
 * are running Tenacity and return a list of valid instances.
 */
public class KubernetesInstanceDiscovery implements InstanceDiscovery {

    private final KubernetesClient client;

    public KubernetesInstanceDiscovery() {
        this.client = new DefaultKubernetesClient();
    }

    public KubernetesInstanceDiscovery(KubernetesClient client) {
        this.client = client;
    }

    @Override
    public Collection<Instance> getInstanceList() throws Exception {
        List<ReplicationController> controllers = client.replicationControllers().list().getItems();
        return controllers.stream().map(c -> {
            String validName = c.getMetadata().getName().toLowerCase().replace(" ", "-");
            String cluster = String.format("%s-%s", c.getMetadata().getNamespace(), validName);
            return new Instance("host", cluster, true);
        }).collect(Collectors.toList());
    }
}
