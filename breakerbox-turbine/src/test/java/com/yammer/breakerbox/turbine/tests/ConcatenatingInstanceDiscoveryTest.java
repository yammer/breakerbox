package com.yammer.breakerbox.turbine.tests;

import com.google.common.collect.ImmutableList;
import com.netflix.turbine.discovery.Instance;
import com.yammer.breakerbox.turbine.ConcatenatingInstanceDiscovery;
import com.yammer.tenacity.testing.TenacityTestRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcatenatingInstanceDiscoveryTest {
    private final Instance instanceOne = new Instance(UUID.randomUUID().toString() + ":12345", "service1", true);
    private final Instance instanceTwo = new Instance(UUID.randomUUID().toString() + ":56789", "service2", true);
    private final Instance instanceThree = new Instance(UUID.randomUUID().toString() + ":57346", "service3", true);
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();

    @Test
    public void twoConcats() throws Exception {
        assertThat(new ConcatenatingInstanceDiscovery(
                () -> ImmutableList.of(instanceOne),
                () -> ImmutableList.of(instanceTwo))
                .getInstanceList())
                .contains(instanceOne, instanceTwo);
    }

    @Test
    public void threeConcats() throws Exception {
        assertThat(new ConcatenatingInstanceDiscovery(
                () -> ImmutableList.of(instanceOne),
                () -> ImmutableList.of(instanceTwo),
                () -> ImmutableList.of(instanceThree))
                .getInstanceList())
                .contains(instanceOne, instanceTwo, instanceThree);
    }
}
