package com.yammer.breakerbox.service.core.tests;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.netflix.turbine.discovery.Instance;
import com.yammer.breakerbox.service.core.Instances;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

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
                .isEqualTo(ImmutableSet.of(new Instance("localhost:8080/tenacity/mock.stream", "mock", true)));
    }
}
