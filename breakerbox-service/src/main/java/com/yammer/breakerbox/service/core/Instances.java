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
import java.net.URISyntaxException;

public class Instances {
    private static final Logger LOGGER = LoggerFactory.getLogger(Instances.class);

    private static Function<Instance, String> toClusterName() {
        return new Function<Instance, String>() {
            @Override
            public String apply(Instance input) {
                return input.getCluster();
            }
        };
    }

    private static Predicate<Instance> pruneMetaClusters() {
        return new Predicate<Instance>() {
            @Override
            public boolean apply(@Nullable Instance input) {
                if (input != null) {
                    switch (input.getCluster().toUpperCase()) {
                        case "PRODUCTION":
                        case "STAGE":
                        case "STAGING":
                            return false;
                        default:
                            return true;
                    }
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
                final URI original = URI.create("http://" + input.getHostname());
                try {
                    return new URI(
                           original.getScheme(),
                           original.getUserInfo(),
                           original.getHost(),
                           original.getPort(),
                           "",
                           original.getQuery(),
                           original.getFragment());
                } catch (URISyntaxException err) {
                    LOGGER.warn("Unexpected exception", err);
                }
                return original;
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

    public static ImmutableSet<String> noMetaClusters() {
        return rawInstances()
                .filter(pruneMetaClusters())
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