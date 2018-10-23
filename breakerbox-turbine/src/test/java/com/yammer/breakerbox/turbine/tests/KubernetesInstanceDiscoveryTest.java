package com.yammer.breakerbox.turbine.tests;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.netflix.turbine.discovery.Instance;
import com.yammer.breakerbox.turbine.KubernetesInstanceDiscovery;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.dsl.ClientMixedOperation;
import io.fabric8.kubernetes.client.dsl.ClientPodResource;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.stub;

@RunWith(MockitoJUnitRunner.class)
public class KubernetesInstanceDiscoveryTest {

    @Mock DefaultKubernetesClient client;
    @Mock ClientMixedOperation<Pod, PodList, DoneablePod, ClientPodResource<Pod, DoneablePod>> podsOperation;
    @Mock PodList podList;

    private KubernetesInstanceDiscovery discovery;
    private List<Pod> pods;

    @Before
    public void setupDiscovery() {
        stub(client.pods()).toReturn(podsOperation);
        stub(podsOperation.list()).toReturn(podList);

        Pod serviceA1 = new Pod();
        serviceA1.setMetadata(new ObjectMeta());
        serviceA1.setStatus(new PodStatus());
        serviceA1.getStatus().setPodIP("10.116.0.6");
        serviceA1.getStatus().setPhase("Running");
        serviceA1.getMetadata().setAnnotations(
                Maps.newHashMap(KubernetesInstanceDiscovery.PORT_ANNOTATION_KEY, "8080"));
        serviceA1.getMetadata().setName("service-a-67das7");
        serviceA1.getMetadata().setGenerateName("service-a-");
        serviceA1.getMetadata().setNamespace("staging");

        Pod serviceA2 = new Pod();
        serviceA2.setMetadata(new ObjectMeta());
        serviceA2.setStatus(new PodStatus());
        serviceA2.getStatus().setPodIP("10.116.0.7");
        serviceA2.getStatus().setPhase("Running");
        serviceA2.getMetadata().setAnnotations(
                Maps.newHashMap(KubernetesInstanceDiscovery.PORT_ANNOTATION_KEY, "8080"));
        serviceA2.getMetadata().setName("service-a-0889d23");
        serviceA2.getMetadata().setGenerateName("service-a-");
        serviceA2.getMetadata().setNamespace("staging");

        Pod serviceB = new Pod();
        serviceB.setMetadata(new ObjectMeta());
        serviceB.setStatus(new PodStatus());
        serviceB.getStatus().setPodIP("10.116.0.8");
        serviceB.getStatus().setPhase("Running");
        serviceB.getMetadata().setAnnotations(
                Maps.newHashMap(KubernetesInstanceDiscovery.PORT_ANNOTATION_KEY, "80"));
        serviceB.getMetadata().setName("service-b-097fsd");
        serviceB.getMetadata().setGenerateName("service-b-");
        serviceB.getMetadata().setNamespace("production");

        // Should not be detected
        Pod nonBreakerboxService = new Pod();
        nonBreakerboxService.setMetadata(new ObjectMeta());
        nonBreakerboxService.setStatus(new PodStatus());
        nonBreakerboxService.getStatus().setPhase("Running");
        nonBreakerboxService.getStatus().setPodIP("10.116.0.9");
        nonBreakerboxService.getMetadata().setAnnotations(new HashMap<>());
        nonBreakerboxService.getMetadata().setName("service-c-097900");
        nonBreakerboxService.getMetadata().setGenerateName("service-c-");
        nonBreakerboxService.getMetadata().setNamespace("production");
        pods = Lists.newArrayList(serviceA1, serviceA2, serviceB, nonBreakerboxService);

        stub(podList.getItems()).toReturn(pods);
        this.discovery = new KubernetesInstanceDiscovery(client);
    }

    @Test
    public void usesOnlyPodsWithPortAnnotation() throws Exception {
        for (Instance instance: discovery.getInstanceList())
            assertThat(instance.getCluster()).doesNotStartWith("production-service-c");
    }

    @Test
    public void usesIpAddressForHostName() throws Exception {
        for (Instance instance: discovery.getInstanceList())
            assertThat(instance.getHostname())
                    .matches(Pattern.compile("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}:[0-9]{2,5}$"));
    }

    @Test
    public void createsCorrectAmountOfInstances() throws Exception {
        assertThat(discovery.getInstanceList().size()).isEqualTo(3);
    }

    @Test
    public void usesPodGenerateNamesAndNamespaceToBuildClusters() throws Exception {
        Collection<Instance> instances = discovery.getInstanceList();
        for (Instance instance: instances) {
            boolean validCluster =
                    instance.getCluster().equals("staging-service-a") ||
                    instance.getCluster().equals("production-service-b");
            assertThat(validCluster).isTrue();
        }
    }

    @Test
    public void onlyAcceptsCorrectValuesInPortAnnotation() throws Exception {
        Pod invalidAnnotation = new Pod();
        invalidAnnotation.setMetadata(new ObjectMeta());
        invalidAnnotation.setStatus(new PodStatus());
        invalidAnnotation.getStatus().setPodIP("10.116.0.8");
        invalidAnnotation.getStatus().setPhase("Running");
        invalidAnnotation.getMetadata().setAnnotations(
                Maps.newHashMap(KubernetesInstanceDiscovery.PORT_ANNOTATION_KEY, "invalid-port"));
        invalidAnnotation.getMetadata().setName("service-invalid-097fsd");
        invalidAnnotation.getMetadata().setGenerateName("service-invalid-");
        invalidAnnotation.getMetadata().setNamespace("production");
        pods.clear();
        pods.add(invalidAnnotation);
        assertThat(discovery.getInstanceList().size()).isEqualTo(0);
    }

    @Test
    public void usesPhasePropertyAsInstanceStatus() throws Exception {
        Pod invalidAnnotation = new Pod();
        invalidAnnotation.setMetadata(new ObjectMeta());
        invalidAnnotation.setStatus(new PodStatus());
        invalidAnnotation.getStatus().setPodIP("10.116.0.8");
        invalidAnnotation.getStatus().setPhase("Preparing");
        invalidAnnotation.getMetadata().setAnnotations(
                Maps.newHashMap(KubernetesInstanceDiscovery.PORT_ANNOTATION_KEY, "8080"));
        invalidAnnotation.getMetadata().setName("service-preparing-097fsd");
        invalidAnnotation.getMetadata().setGenerateName("service-preparing-");
        invalidAnnotation.getMetadata().setNamespace("production");
        pods.add(invalidAnnotation);
        for (Instance instance: discovery.getInstanceList()) {
            if (instance.getCluster().equals("production-service-preparing"))
                assertThat(instance.isUp()).isFalse();
            else
                assertThat(instance.isUp()).isTrue();
        }
    }

    @Test
    public void removesPodTemplateHashFromClusterName() throws Exception {
        Pod deploymentPod = new Pod();
        deploymentPod.setMetadata(new ObjectMeta());
        deploymentPod.setStatus(new PodStatus());
        deploymentPod.getStatus().setPodIP("10.116.0.8");
        deploymentPod.getStatus().setPhase("Running");
        deploymentPod.getMetadata().setAnnotations(
                Maps.newHashMap(KubernetesInstanceDiscovery.PORT_ANNOTATION_KEY, "8080"));
        deploymentPod.getMetadata().setLabels(
                Maps.newHashMap(KubernetesInstanceDiscovery.POD_HASH_LABEL_KEY, "5432543253"));
        deploymentPod.getMetadata().setName("service-depl-5432543253-097fsd");
        deploymentPod.getMetadata().setGenerateName("service-depl-5432543253-");
        deploymentPod.getMetadata().setNamespace("production");
        pods.add(deploymentPod);
        Optional<Instance> instanceOptional = discovery.getInstanceList().stream()
                .filter(instance -> instance.getCluster().equals("production-service-depl"))
                .findAny();
        assertThat(instanceOptional.isPresent()).isTrue();
    }

    @Test
    public void prefersAppLabelToExtractedName() throws Exception {
        Pod deploymentPod = new Pod();
        deploymentPod.setMetadata(new ObjectMeta());
        deploymentPod.setStatus(new PodStatus());
        deploymentPod.getStatus().setPodIP("10.116.0.8");
        deploymentPod.getStatus().setPhase("Running");
        deploymentPod.getMetadata().setAnnotations(
                Maps.newHashMap(KubernetesInstanceDiscovery.PORT_ANNOTATION_KEY, "8080"));
        deploymentPod.getMetadata().setLabels(ImmutableMap.of(
                KubernetesInstanceDiscovery.POD_HASH_LABEL_KEY, "5432543253",
                KubernetesInstanceDiscovery.APP_LABEL_KEY, "test-service")
        );
        deploymentPod.getMetadata().setName("service-depl-5432543253-097fsd");
        deploymentPod.getMetadata().setGenerateName("service-depl-5432543253-");
        deploymentPod.getMetadata().setNamespace("production");
        pods.add(deploymentPod);
        Optional<Instance> instanceOptional = discovery.getInstanceList().stream()
                .filter(instance -> instance.getCluster().equals("test-service"))
                .findAny();
        assertThat(instanceOptional.isPresent()).isTrue();

    }
}
