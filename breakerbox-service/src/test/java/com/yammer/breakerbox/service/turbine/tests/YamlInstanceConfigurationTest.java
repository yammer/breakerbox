package com.yammer.breakerbox.service.turbine.tests;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.google.common.net.HostAndPort;
import com.netflix.turbine.discovery.Instance;
import com.yammer.breakerbox.service.turbine.YamlInstanceConfiguration;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class YamlInstanceConfigurationTest {
    private ConfigurationFactory<YamlInstanceConfiguration> configFactory = new ConfigurationFactory<>(
            YamlInstanceConfiguration.class,
            Validators.newValidator(),
            Jackson.newObjectMapper(),
            "dw");

    @Test
    public void defaultConfiguration() throws Exception {
        final YamlInstanceConfiguration configuration = configFactory.build(
                new File(Resources.getResource("turbineConfigurations/default.yml").toURI()));
        assertThat(configuration.getClusters()).isEqualTo(ImmutableMap.of(
                "breakerbox", YamlInstanceConfiguration.Cluster.withInstances(HostAndPort.fromParts("localhost", 8080)),
                "production", YamlInstanceConfiguration.Cluster.withClusters("breakerbox")));
        assertThat(configuration.getAllInstances()).isEqualTo(ImmutableSet.of(
                new Instance("localhost:8080", "breakerbox", true),
                new Instance("localhost:8080", "production", true)));
    }

    @Test
    public void multipleClusters() throws Exception {
        final YamlInstanceConfiguration configuration = configFactory.build(
                new File(Resources.getResource("turbineConfigurations/multipleClusters.yml").toURI()));
        assertThat(configuration.getClusters()).isEqualTo(ImmutableMap.of(
                "one", new YamlInstanceConfiguration.Cluster(
                        ImmutableSet.of(HostAndPort.fromParts("localhost", 1234), HostAndPort.fromParts("localhost", 5678)),
                        ImmutableSet.of("two")),
                "two", new YamlInstanceConfiguration.Cluster(
                        ImmutableSet.of(HostAndPort.fromParts("localhost", 4321), HostAndPort.fromParts("localhost", 9876)),
                        ImmutableSet.of("one")),
                "three", YamlInstanceConfiguration.Cluster.withClusters("one", "two")));
        assertThat(configuration.getAllInstances()).isEqualTo(
                ImmutableSet.of(
                        new Instance("localhost:1234", "one", true), new Instance("localhost:5678", "one", true),
                        new Instance("localhost:4321", "one", true), new Instance("localhost:9876", "one", true),
                        new Instance("localhost:4321", "two", true), new Instance("localhost:9876", "two", true),
                        new Instance("localhost:1234", "two", true), new Instance("localhost:5678", "two", true),
                        new Instance("localhost:4321", "three", true), new Instance("localhost:9876", "three", true),
                        new Instance("localhost:1234", "three", true), new Instance("localhost:5678", "three", true)));
    }

    @Test
    public void selfReference() throws Exception {
        final YamlInstanceConfiguration configuration = configFactory.build(
                new File(Resources.getResource("turbineConfigurations/selfReference.yml").toURI()));
        assertThat(configuration.getClusters()).isEqualTo(ImmutableMap.of(
                "one", new YamlInstanceConfiguration.Cluster(
                        ImmutableSet.of(HostAndPort.fromParts("localhost", 1234)),
                        ImmutableSet.of("one"))));
        assertThat(configuration.getAllInstances()).isEqualTo(ImmutableSet.of(new Instance("localhost:1234", "one", true)));
    }

    @Test
    public void urlSuffix() throws Exception {
        final YamlInstanceConfiguration configuration = configFactory.build(
                new File(Resources.getResource("turbineConfigurations/urlSuffix.yml").toURI()));
        assertThat(configuration.getUrlSuffix()).isEqualTo("/foo/bar");
    }
}
