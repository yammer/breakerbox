package com.yammer.breakerbox.service.views;

import com.google.common.net.HostAndPort;
import com.google.common.net.UrlEscapers;

import java.util.Set;

public class DashboardView extends NavbarView {
    private final String clusterName;
    private final HostAndPort breakerboxHostAndPort;

    public DashboardView(String clusterName, HostAndPort breakerboxHostAndPort, Set<String> specifiedMetaClusters) {
        super("/templates/dashboard/dashboard.mustache", specifiedMetaClusters);
        this.clusterName = clusterName;
        this.breakerboxHostAndPort = breakerboxHostAndPort;
    }

    public String escapedBreakerboxHostAndPort() {
        return UrlEscapers.urlFormParameterEscaper().escape(breakerboxHostAndPort.toString());
    }

    public HostAndPort getBreakerboxHostAndPort() {
        return breakerboxHostAndPort;
    }

    public String getClusterName() {
        return clusterName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DashboardView that = (DashboardView) o;

        if (!breakerboxHostAndPort.equals(that.breakerboxHostAndPort)) return false;
        if (!clusterName.equals(that.clusterName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = clusterName.hashCode();
        result = 31 * result + breakerboxHostAndPort.hashCode();
        return result;
    }
}