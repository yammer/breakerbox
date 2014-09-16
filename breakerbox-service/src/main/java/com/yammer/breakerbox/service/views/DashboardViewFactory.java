package com.yammer.breakerbox.service.views;

import com.google.common.net.HostAndPort;

public class DashboardViewFactory {
    private final HostAndPort breakerboxHostAndPort;

    public DashboardViewFactory(HostAndPort breakerboxHostAndPort) {
        this.breakerboxHostAndPort = breakerboxHostAndPort;
    }

    public DashboardView create(String clusterName) {
        return new DashboardView(clusterName, breakerboxHostAndPort);
    }
}