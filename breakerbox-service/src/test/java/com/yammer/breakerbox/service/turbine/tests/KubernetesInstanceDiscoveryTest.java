package com.yammer.breakerbox.service.turbine.tests;

import com.google.common.collect.Lists;
import com.netflix.turbine.discovery.Instance;
import com.yammer.breakerbox.service.turbine.KubernetesInstanceDiscovery;
import io.fabric8.kubernetes.api.model.DoneableReplicationController;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.ReplicationControllerList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.dsl.ClientMixedOperation;
import io.fabric8.kubernetes.client.dsl.ClientRollableScallableResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.stub;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class KubernetesInstanceDiscoveryTest {

    @Mock DefaultKubernetesClient client;
    @Mock ClientMixedOperation<
            ReplicationController,
            ReplicationControllerList,
            DoneableReplicationController,
            ClientRollableScallableResource<ReplicationController, DoneableReplicationController>> controllers;
    @Mock ReplicationControllerList controllerList;

    private KubernetesInstanceDiscovery discovery;

    @Before
    public void setupDiscovery() {
        ReplicationController serviceA = new ReplicationController();
        serviceA.setMetadata(new ObjectMeta());
        serviceA.getMetadata().setName("Service A");
        serviceA.getMetadata().setNamespace("staging");
        ReplicationController serviceB = new ReplicationController();
        serviceB.setMetadata(new ObjectMeta());
        serviceB.getMetadata().setName("Service B");
        serviceB.getMetadata().setNamespace("production");
        List<ReplicationController> list = Lists.newArrayList(serviceA, serviceB);

        stub(client.replicationControllers()).toReturn(controllers);
        stub(controllers.list()).toReturn(controllerList);
        stub(controllerList.getItems()).toReturn(list);
        this.discovery = new KubernetesInstanceDiscovery(client);
    }

    @Test
    public void usesControllerNamesToBuildClusters() throws Exception {
        Collection<Instance> instances = discovery.getInstanceList();
        for (Instance instance: instances) {
            boolean validCluster =
                    instance.getCluster().equals("staging-service-a") ||
                    instance.getCluster().equals("production-service-b");
            assertThat(validCluster).isTrue();
        }

    }
}
