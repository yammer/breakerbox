package com.yammer.breakerbox.turbine;


import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Discovers instances in a Kubernetes cluster. The idea is to
 * find pods with the 'breakerbox-port' annotation and use their
 * base names in combination with the namespace to build Hystrix clusters.
 * Example:
 * A pod named 'service-5ujh1' in the namespace 'staging' would be in the cluster 'staging-service',
 * together with all pods created by the same ReplicationController or ReplicaSet.
 */
public class KubernetesInstanceDiscovery implements InstanceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesInstanceDiscovery.class);

    public static final String PORT_ANNOTATION_KEY = "breakerbox-port";
    public static final String POD_HASH_LABEL_KEY = "pod-template-hash";
    public static final String APP_LABEL_KEY = "app";

    private final KubernetesClient client;

    public KubernetesInstanceDiscovery() {
        this.client = new DefaultKubernetesClient();
    }

    public KubernetesInstanceDiscovery(KubernetesClient client) {
        this.client = client;
    }

    @Override
    public Collection<Instance> getInstanceList() throws Exception {
        LOGGER.info("Starting Kubernetes instance discovery using master URL: {}", client.getMasterUrl());
        return client.pods().inAnyNamespace()
                .list()
                .getItems().stream()
                .filter(pod -> pod.getMetadata().getAnnotations() != null)  // Ignore pods without annotations
                .filter(pod -> pod.getMetadata().getAnnotations().containsKey(PORT_ANNOTATION_KEY))
                .map(pod -> {
                    String portString = pod.getMetadata().getAnnotations().get(PORT_ANNOTATION_KEY);
                    if (!Pattern.compile("^[0-9]{2,5}$").matcher(portString).matches()) {
                        LOGGER.warn("Invalid port annotation for pod '{}': {}", pod.getMetadata().getName(), portString);
                        return null;
                    } else {
                        String host = String.format("%s:%s", pod.getStatus().getPodIP(), portString);
                        boolean running = pod.getStatus().getPhase().equals("Running");
                        LOGGER.info("Found Kubernetes Pod {} at address {}", pod.getMetadata().getName(), host);
                        return new Instance(host, extractClusterNameFor(pod), running);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static String extractClusterNameFor(Pod pod) {
        String podBaseName = pod.getMetadata().getGenerateName();
        // Remove auto-generated hashes, if there are any
        if (pod.getMetadata().getLabels() != null) {
            if (pod.getMetadata().getLabels().containsKey(APP_LABEL_KEY)) {
                return pod.getMetadata().getLabels().get(APP_LABEL_KEY);
            }
            if (pod.getMetadata().getLabels().containsKey(POD_HASH_LABEL_KEY)) {
                String hash = pod.getMetadata().getLabels().get(POD_HASH_LABEL_KEY);
                podBaseName = podBaseName.replace(hash + "-", "");
            }
        }
        // Pod's base names always end with a '-', remove it
        podBaseName = podBaseName.substring(0, podBaseName.length() - 1);
        return String.format("%s-%s", pod.getMetadata().getNamespace(), podBaseName);
    }
}
