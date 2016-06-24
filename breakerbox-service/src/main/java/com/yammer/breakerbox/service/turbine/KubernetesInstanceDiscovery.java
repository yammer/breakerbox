package com.yammer.breakerbox.service.turbine;


import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Discovers instances in a Kubernetes cluster. The idea is to
 * find ReplicationControllers or ReplicaSets, check if their instances
 * are running Tenacity and return a list of valid instances.
 */
public class KubernetesInstanceDiscovery implements InstanceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesInstanceDiscovery.class);

    public static final String PORT_ANNOTATION_KEY = "breakerbox-port";

    private final KubernetesClient client;

    public KubernetesInstanceDiscovery() {
        this.client = new DefaultKubernetesClient();
    }

    public KubernetesInstanceDiscovery(KubernetesClient client) {
        this.client = client;
    }

    @Override
    public Collection<Instance> getInstanceList() throws Exception {
        return client.pods().inAnyNamespace()
                .list()
                .getItems().stream()
                .filter(pod -> pod.getMetadata().getAnnotations().containsKey(PORT_ANNOTATION_KEY))
                .map(pod -> {
                    String podBaseName = pod.getMetadata().getGenerateName();
                    String cluster = String.format("%s-%s",
                            pod.getMetadata().getNamespace(),
                            // Pod's base names always end with a '-', remove it
                            podBaseName.substring(0, podBaseName.length()-1));
                    String portString = pod.getMetadata().getAnnotations().get(PORT_ANNOTATION_KEY);
                    if (!Pattern.compile("^[0-9]{2,5}$").matcher(portString).matches()) {
                        LOGGER.warn("Invalid port annotation for pod '{}': {}", pod.getMetadata().getName(), portString);
                        return null;
                    } else {
                        String host = String.format("%s:%s", pod.getStatus().getPodIP(), portString);
                        boolean running = pod.getStatus().getPhase().equals("Running");
                        return new Instance(host, cluster, running);
                    }
                })
                .filter(pod -> pod != null)
                .collect(Collectors.toList());
    }
}
