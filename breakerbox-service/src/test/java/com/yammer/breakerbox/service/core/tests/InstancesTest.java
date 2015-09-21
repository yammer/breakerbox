package com.yammer.breakerbox.service.core.tests;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.netflix.turbine.discovery.Instance;
import com.yammer.breakerbox.service.core.Instances;
import com.yammer.breakerbox.store.ServiceId;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class InstancesTest {
    @BeforeClass
    public static void setupTest() throws Exception {
        System.getProperties().load(
                new FileInputStream(new File(Resources.getResource("config.properties").toURI())));
    }

    @Test
    public void clusters() {
        assertThat(Instances.clusters())
                .isEqualTo(ImmutableSet.of("production", "mock"));
    }

    @Test
    public void instances() {
        assertThat(Instances.noMetaClusters())
                .isEqualTo(ImmutableSet.of("mock"));
    }

    @Test
    public void propertyKeyUris() {
        assertThat(Instances.propertyKeyUris())
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
                .isEqualTo(ImmutableSet.of(new Instance("localhost:8080", "mock", true)));
    }

    @Test
    public void propertyKeysUrisForCluster() {
        assertThat(Instances.propertyKeyUris(ServiceId.from("mock")))
                .isEqualTo(ImmutableSet.of(URI.create("http://localhost:8080")));
    }
}
