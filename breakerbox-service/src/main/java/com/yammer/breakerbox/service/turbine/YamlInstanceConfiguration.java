package com.yammer.breakerbox.service.turbine;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HostAndPort;
import com.netflix.turbine.discovery.Instance;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class YamlInstanceConfiguration {
    @JsonUnwrapped
    @Valid
    private ImmutableMap<String, Cluster> clusters = ImmutableMap.of(
            "breakerbox", Cluster.withInstances(HostAndPort.fromParts("localhost", 8080)),
            "production", Cluster.withClusters("breakerbox"));

    @NotNull
    private String urlSuffix = "/tenacity/metrics.stream";

    public ImmutableMap<String, Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(ImmutableMap<String, Cluster> clusters) {
        this.clusters = clusters;
    }

    public Set<Instance> getAllInstances() {
        final ImmutableSet.Builder<Instance> builder = ImmutableSet.builder();
        for (Map.Entry<String, Cluster> entry : clusters.entrySet()) {
            final String clusterName = entry.getKey();
            addCluster(builder, entry.getValue(), clusterName, ImmutableSet.of(clusterName));
        }
        return builder.build();
    }

    private void addCluster(ImmutableSet.Builder<Instance> acc,
                            Cluster cluster,
                            String clusterName,
                            ImmutableSet<String> visited) {
        acc.addAll(cluster.getInstances().stream()
                .map(hostAndPort -> new Instance(hostAndPort.toString(), clusterName, true))
                .collect(Collectors.toList()));
        cluster.getClusters()
                .stream()
                .filter(name -> !visited.contains(name))
                .forEach(notVisitedClusterName ->
                    addCluster(acc, clusters.get(notVisitedClusterName), clusterName,
                        ImmutableSet.<String>builder().addAll(visited).add(notVisitedClusterName).build()));
    }

    public String getUrlSuffix() {
        return urlSuffix;
    }

    public void setUrlSuffix(String urlSuffix) {
        this.urlSuffix = urlSuffix;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusters, urlSuffix);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final YamlInstanceConfiguration other = (YamlInstanceConfiguration) obj;
        return Objects.equals(this.clusters, other.clusters)
                && Objects.equals(this.urlSuffix, other.urlSuffix);
    }

    @Override
    public String toString() {
        return "YamlInstanceConfiguration{" +
                "clusters=" + clusters +
                ", urlSuffix='" + urlSuffix + '\'' +
                '}';
    }

    public static class Cluster {
        @Valid
        private Set<HostAndPort> instances = Collections.emptySet();
        private Set<String> clusters = Collections.emptySet();

        public Cluster() { /* Jackson */
        }

        public Cluster(Set<HostAndPort> instances, Set<String> clusters) {
            this.instances = instances;
            this.clusters = clusters;
        }

        public Set<HostAndPort> getInstances() {
            return instances;
        }

        public Set<String> getClusters() {
            return clusters;
        }

        public void setInstances(Set<HostAndPort> instances) {
            this.instances = instances;
        }

        public void setClusters(Set<String> clusters) {
            this.clusters = clusters;
        }

        public static Cluster withInstances(HostAndPort... hostAndPorts) {
            return new Cluster(ImmutableSet.copyOf(hostAndPorts), Collections.<String>emptySet());
        }

        public static Cluster withClusters(String... clusters) {
            return new Cluster(Collections.<HostAndPort>emptySet(), ImmutableSet.copyOf(clusters));
        }

        @Override
        public int hashCode() {
            return Objects.hash(instances, clusters);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final Cluster other = (Cluster) obj;
            return Objects.equals(this.instances, other.instances)
                    && Objects.equals(this.clusters, other.clusters);
        }

        @Override
        public String toString() {
            return "Cluster{" +
                    "instances=" + instances +
                    ", clusters=" + clusters +
                    '}';
        }
    }
}
