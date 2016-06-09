package com.yammer.breakerbox.service.turbine.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.InetAddresses;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.plugins.PluginsFactory;
import com.yammer.breakerbox.service.turbine.LodbrokInstanceDiscovery;
import com.yammer.lodbrok.discovery.core.HealthCheck;
import com.yammer.lodbrok.discovery.core.LodbrokInstance;
import com.yammer.lodbrok.discovery.core.PortMapping;
import com.yammer.lodbrok.discovery.core.Task;
import com.yammer.lodbrok.discovery.core.store.LodbrokInstanceStore;
import com.yammer.tenacity.testing.TenacityTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class LodbrokInstanceDiscoveryTest {
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();
    private final URI lodbrokGlobalUri = URI.create("https://some.where.io:10014");
    private LodbrokInstanceStore lodbrokInstanceStore;
    private LodbrokInstanceDiscovery discovery;
    private final Task task1 = new Task(
            "service1-" + UUID.randomUUID().toString(),
            InetAddresses.forString("1.2.3.4"),
            "service1",
            ImmutableList.of("12345"),
            "Running");
    private final Task task2 = new Task(
            "service2-" + UUID.randomUUID().toString(),
            InetAddresses.forString("5.6.7.8"),
            "service2",
            ImmutableList.of("56789"),
            "Running");
    private final Task task3 = new Task(
            "service3-" + UUID.randomUUID().toString(),
            InetAddresses.forString("1.2.3.4"),
            "service3",
            ImmutableList.of("12345"),
            "Running");
    private final LodbrokInstance instance1 = new LodbrokInstance(
            ImmutableMap.of("service1", task1),
            "service1",
            ImmutableList.of(new PortMapping("0", "0", "12345", "tcp")),
            "http",
            "",
            false,
            false,
            ImmutableList.of(new HealthCheck(20, false, 3, 3, "", 0, "TCP", 3)));
    private final LodbrokInstance instance2 = new LodbrokInstance(
            ImmutableMap.of("service3", task3, "service2", task2),
            "service2",
            ImmutableList.of(new PortMapping("0", "0", "56789", "tcp")),
            "http",
            "",
            false,
            false,
            ImmutableList.of(new HealthCheck(20, false, 3, 3, "", 0, "TCP", 3)));

    @Before
    public void setup() throws Exception {
        lodbrokInstanceStore = LodbrokInstanceStore.empty();
        discovery = new LodbrokInstanceDiscovery(lodbrokInstanceStore, lodbrokGlobalUri);
        PluginsFactory.setInstanceDiscovery(discovery);
        assertThat(discovery.getInstanceList()).isEmpty();
    }

    @Test
    public void empty() throws Exception {
        assertThat(discovery.getInstanceList()).isEmpty();
    }

    @Test
    public void oneInstance() throws Exception {
        lodbrokInstanceStore.merge(ImmutableList.of(instance1));
        assertThat(discovery.getInstanceList()).containsExactly(instanceOne());
    }

    @Test
    public void twoInstancesOnSameHost() throws Exception {
        lodbrokInstanceStore.merge(ImmutableList.of(instance2));
        assertThat(discovery.getInstanceList()).containsOnly(instanceThree(), instanceTwo());
    }


    @Test
    public void twoInstancesOnDifferentHosts() throws Exception {
        lodbrokInstanceStore.merge(ImmutableList.of(instance1, instance2));
        assertThat(discovery.getInstanceList()).containsOnly(instanceOne(), instanceTwo(), instanceThree());
    }

    private Instance instanceOne() {
        final Instance instance = new Instance("https://some.where.io:12345", "service1", true);
        instance.getAttributes().putIfAbsent(LodbrokInstanceDiscovery.LODBROK_ROUTE,
                String.format("%s-%s", "1.2.3.4", task1.getId()));
        return instance;
    }

    private Instance instanceTwo() {
        final Instance instance = new Instance("https://some.where.io:56789", "service2", true);
        instance.getAttributes().putIfAbsent(LodbrokInstanceDiscovery.LODBROK_ROUTE,
                String.format("%s-%s", "5.6.7.8", task2.getId()));
        return instance;
    }

    private Instance instanceThree() {
        final Instance instance = new Instance("https://some.where.io:12345", "service3", true);
        instance.getAttributes().putIfAbsent(LodbrokInstanceDiscovery.LODBROK_ROUTE,
                String.format("%s-%s", "1.2.3.4", task3.getId()));
        return instance;
    }
}
