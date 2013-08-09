package com.yammer.breakerbox.service.views;

import com.google.common.collect.ImmutableCollection;
import com.yammer.breakerbox.service.core.Instances;
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
        return Instances.clusters();
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