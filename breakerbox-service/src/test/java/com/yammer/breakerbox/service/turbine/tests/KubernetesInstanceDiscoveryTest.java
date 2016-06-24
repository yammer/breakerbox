package com.yammer.breakerbox.service.turbine.tests;

import com.google.common.collect.Lists;
import com.netflix.turbine.discovery.Instance;
import com.yammer.breakerbox.service.turbine.KubernetesInstanceDiscovery;
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
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.stub;

@RunWith(MockitoJUnitRunner.class)
public class KubernetesInstanceDiscoveryTest {

    @Mock DefaultKubernetesClient client;
    @Mock ClientMixedOperation<Pod, PodList, DoneablePod, ClientPodResource<Pod, DoneablePod>> podsOperation;
    @Mock ClientMixedOperation<Pod, PodList, DoneablePod, ClientPodResource<Pod, DoneablePod>> podsAnyNsOperation;
    @Mock PodList podList;

    private KubernetesInstanceDiscovery discovery;

    @Before
    public void setupDiscovery() {
        stub(client.pods()).toReturn(podsOperation);
        stub(podsOperation.inAnyNamespace()).toReturn(podsAnyNsOperation);
        stub(podsAnyNsOperation.list()).toReturn(podList);

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
        List<Pod> pods = Lists.newArrayList(serviceA1, serviceA2, serviceB, nonBreakerboxService);

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
    public void onlyAcceptsCorrectValuesInPortAnnotation() {
        // TODO: Only valid port values should be accepted
    }

    @Test
    public void usesPhasePropertyAsInstanceStatus() {
        // TODO: Mark instance as 'active' if pod phase is 'Running'
    }
}
