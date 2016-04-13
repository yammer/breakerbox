package com.yammer.breakerbox.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HostAndPort;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Set;

@Path("/")
public class DashboardResource {
    private final String defaultDashboard;
    private final HostAndPort breakerboxHostAndPort;
    private final Set<String> specifiedMetaClusters;

    public DashboardResource(String defaultDashboard, HostAndPort breakerboxHostAndPort, Set<String> specifiedMetaClusters) {
        this.defaultDashboard = defaultDashboard;
        this.breakerboxHostAndPort = breakerboxHostAndPort;
        this.specifiedMetaClusters = specifiedMetaClusters;
    }

    @Path("/dashboard") @GET @Timed @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> defaultDashboard() {
        return ImmutableMap.of(
                "name", defaultDashboard,
                "turbine", breakerboxHostAndPort,
                "metaClusters", specifiedMetaClusters);
    }
}