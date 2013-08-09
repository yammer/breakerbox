package com.yammer.breakerbox.service.core.tests;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.netflix.turbine.discovery.Instance;
import com.yammer.breakerbox.service.core.Instances;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

import static org.fest.assertions.api.Assertions.assertThat;

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
        assertThat(Instances.instances())
                .isEqualTo(ImmutableSet.of(new Instance("http://localhost:8080/tenacity/mock.stream", "mock", true)));
    }

    @Test
    public void propertyKeyUris() {
        assertThat(Instances.propertyKeyUris())
                .isEqualTo(ImmutableSet.of(
                        URI.create("http://localhost:8080/tenacity/propertykeys"),
                        URI.create("http://completie-001.sjc1.yammer.com:8080/tenacity/propertykeys"),
                        URI.create("http://completie-002.sjc1.yammer.com:8080/tenacity/propertykeys"),
                        URI.create("http://completie-003.sjc1.yammer.com:8080/tenacity/propertykeys"),
                        URI.create("http://completie-004.sjc1.yammer.com:8080/tenacity/propertykeys"),
                        URI.create("http://deploy-001.sjc1.yammer.com:9090/tenacity/propertykeys")));
    }
}
