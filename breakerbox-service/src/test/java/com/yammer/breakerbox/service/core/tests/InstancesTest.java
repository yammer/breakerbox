package com.yammer.breakerbox.service.core.tests;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.plugins.PluginsFactory;
import com.yammer.breakerbox.service.core.Instances;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.turbine.YamlInstanceDiscovery;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class InstancesTest {
    @BeforeClass
    public static void setupTest() throws Exception {
        PluginsFactory.setInstanceDiscovery(new YamlInstanceDiscovery(
                Paths.get(Resources.getResource("turbineConfigurations/instances.yml").toURI()),
                Validators.newValidator(),
                Jackson.newObjectMapper()));
    }

    @Test
    public void clusters() {
        assertThat(Instances.clusters())
                .isEqualTo(ImmutableSet.of("production", "mock"));
    }

    @Test
    public void instances() {
        Set<String> specifiedMetaClusters = Sets.newHashSet("PRODUCTION");
        assertThat(Instances.noMetaClusters(specifiedMetaClusters))
                .isEqualTo(ImmutableSet.of("mock"));
    }

    @Test
    public void propertyKeyUris() {
        assertThat(Instances
                .instances()
                .stream()
                .map((instance) -> URI.create("http://" + instance.getHostname()))
                .collect(Collectors.toSet()))
                .isEqualTo(ImmutableSet.of(
                        URI.create("http://localhost:8080"),
                        URI.create("http://completie-001.sjc1.yammer.com:8080"),
                        URI.create("http://completie-002.sjc1.yammer.com:8080"),
                        URI.create("http://completie-003.sjc1.yammer.com:8080"),
                        URI.create("http://completie-004.sjc1.yammer.com:8080"),
                        URI.create("http://deploy-001.sjc1.yammer.com:9090")));
    }

    @Test
    public void instancesForCluster() {
        assertThat(Instances.instances(ServiceId.from("mock")))
                .containsExactly(new Instance("localhost:8080", "mock", true));
    }

    @Test
    public void propertyKeysUrisForCluster() {
        assertThat(Instances
                .instances(ServiceId.from("mock"))
                .stream()
                .map((instance) -> URI.create("http://" + instance.getHostname()))
                .collect(Collectors.toSet()))
                .isEqualTo(ImmutableSet.of(URI.create("http://localhost:8080")));
    }
}
