package com.yammer.breakerbox.service.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashboardView extends NavbarView {
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardView.class);
    private final String clusterName;

    public DashboardView(String clusterName) {
        super("/templates/dashboard/dashboard.mustache");
        this.clusterName = clusterName;
    }

    public String getClusterName() {
        return clusterName;
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