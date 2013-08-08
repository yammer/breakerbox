package com.yammer.breakerbox.service.views;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.netflix.turbine.discovery.ConfigPropertyBasedDiscovery;
import com.netflix.turbine.discovery.Instance;
import com.yammer.dropwizard.views.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashboardView extends View {
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardView.class);
    private final String clusterName;

    public DashboardView(String clusterName) {
        super("/templates/dashboard/dashboard.mustache");
        this.clusterName = clusterName;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public String getClusterName() {
        return clusterName;
    }

    public ImmutableCollection<String> getClusters() {
        final ImmutableSet.Builder<String> clusters = ImmutableSet.builder();
        final ConfigPropertyBasedDiscovery configPropertyBasedDiscovery = new ConfigPropertyBasedDiscovery();
        try {
            for (Instance instance : configPropertyBasedDiscovery.getInstanceList()) {
                clusters.add(instance.getCluster());
            }
        } catch (Exception err) {
            LOGGER.warn("Could not fetch clusters dynamically", err);
        }

        return Ordering.natural().immutableSortedCopy(clusters.build());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DashboardView that = (DashboardView) o;

        if (!clusterName.equals(that.clusterName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return clusterName.hashCode();
    }
}