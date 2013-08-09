package com.yammer.breakerbox.service.core;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.netflix.turbine.discovery.ConfigPropertyBasedDiscovery;
import com.netflix.turbine.discovery.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collections;

public class Instances {
    private static final Logger LOGGER = LoggerFactory.getLogger(Instances.class);

    private static Function<Instance, String> clusterNameFun() {
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

    private static FluentIterable<Instance> rawInstances() {
        final ConfigPropertyBasedDiscovery configPropertyBasedDiscovery = new ConfigPropertyBasedDiscovery();
        try {
            return FluentIterable.from(configPropertyBasedDiscovery.getInstanceList());
        } catch (Exception err) {
            LOGGER.warn("Could not fetch clusters dynamically", err);
        }

        return FluentIterable.from(Collections.<Instance>emptyList());
    }

    public static ImmutableSet<String> clusters() {
        return rawInstances()
                .transform(clusterNameFun())
                .toSortedSet(Ordering.natural());
    }

    public static ImmutableSet<Instance> instances() {
        return rawInstances()
                .filter(pruneMetaClusters())
                .toSortedSet(Ordering.natural());
    }
}