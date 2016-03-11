package com.yammer.breakerbox.service.views;

import com.google.common.net.HostAndPort;

import java.util.Set;

public class DashboardViewFactory {
    private final HostAndPort breakerboxHostAndPort;

    public DashboardViewFactory(HostAndPort breakerboxHostAndPort) {
        this.breakerboxHostAndPort = breakerboxHostAndPort;
    }

    public DashboardView create(String clusterName, Set<String> specifiedMetaClusters) {
        return new DashboardView(clusterName, breakerboxHostAndPort, specifiedMetaClusters);
    }
}