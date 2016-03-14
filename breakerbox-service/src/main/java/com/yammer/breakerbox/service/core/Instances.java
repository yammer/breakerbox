package com.yammer.breakerbox.service.core;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.netflix.turbine.discovery.ConfigPropertyBasedDiscovery;
import com.netflix.turbine.discovery.Instance;
import com.yammer.breakerbox.store.ServiceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class Instances {
    private static final Logger LOGGER = LoggerFactory.getLogger(Instances.class);

    private static Function<Instance, String> toClusterName() {
        return new Function<Instance, String>() {
            @Override
            public String apply(Instance input) {
                return checkNotNull(input).getCluster();
            }
        };
    }

    private static Predicate<Instance> pruneMetaClusters(final Set<String> specifiedMetaClusters) {
        return new Predicate<Instance>() {
            @Override
            public boolean apply(@Nullable Instance input) {
                if (input != null) {
                    return !specifiedMetaClusters.contains(input.getCluster().toUpperCase());
                }
                return false;
            }
        };
    }

    public static Predicate<Instance> filterClusterOnly(final String clusterName) {
        return new Predicate<Instance>() {
            @Override
            public boolean apply(@Nullable Instance input) {
                return input != null && input.getCluster().equals(clusterName);
            }
        };
    }

    private static Function<Instance, URI> toPropertyKeyUri() {
        return new Function<Instance, URI>() {
            @Override
            public URI apply(Instance input) {
                return URI.create("http://" + input.getHostname().trim());
            }
        };
    }

    private static FluentIterable<Instance> rawInstances() {
        final ConfigPropertyBasedDiscovery configPropertyBasedDiscovery = new ConfigPropertyBasedDiscovery();
        try {
            return FluentIterable.from(configPropertyBasedDiscovery.getInstanceList());
        } catch (Exception err) {
            LOGGER.warn("Could not fetch clusters dynamically", err);
        }

        return FluentIterable.from(ImmutableList.<Instance>of());
    }

    public static ImmutableSet<String> clusters() {
        return rawInstances()
                .transform(toClusterName())
                .toSortedSet(Ordering.natural());
    }

    public static ImmutableSet<String> noMetaClusters(final Set<String> specifiedMetaClusters) {
        return rawInstances()
                .filter(pruneMetaClusters(specifiedMetaClusters))
                .transform(toClusterName())
                .toSortedSet(Ordering.natural());
    }

    public static ImmutableSet<Instance> instances(ServiceId serviceId) {
        return rawInstances()
                .filter(filterClusterOnly(serviceId.getId()))
                .toSet();
    }

    public static ImmutableSet<URI> propertyKeyUris(ServiceId serviceId) {
        return rawInstances()
                .filter(filterClusterOnly(serviceId.getId()))
                .transform(toPropertyKeyUri())
                .toSet();
    }

    public static ImmutableSet<URI> propertyKeyUris() {
        return rawInstances()
                .transform(toPropertyKeyUri())
                .toSet();
    }
}