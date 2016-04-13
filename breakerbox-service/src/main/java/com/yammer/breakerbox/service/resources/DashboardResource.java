package com.yammer.breakerbox.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HostAndPort;
import com.yammer.breakerbox.service.views.DashboardView;
import com.yammer.breakerbox.service.views.DashboardViewFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Set;

@Path("/")
public class DashboardResource {
    private final String defaultDashboard;
    private final HostAndPort breakerboxHostAndPort;
    private final DashboardViewFactory viewFactory;
    private final Set<String> specifiedMetaClusters;

    public DashboardResource(String defaultDashboard, HostAndPort breakerboxHostAndPort, DashboardViewFactory viewFactory, Set<String> specifiedMetaClusters) {
        this.defaultDashboard = defaultDashboard;
        this.breakerboxHostAndPort = breakerboxHostAndPort;
        this.viewFactory = viewFactory;
        this.specifiedMetaClusters = specifiedMetaClusters;
    }

    @GET @Timed @Produces(MediaType.TEXT_HTML)
    public DashboardView render(@QueryParam("cluster") Optional<String> clusterName) {
        return viewFactory.create(clusterName.or(defaultDashboard), specifiedMetaClusters);
    }

    @Path("/dashboard") @GET @Timed @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> defaultDashboard() {
        return ImmutableMap.of(
                "name", defaultDashboard,
                "turbine", breakerboxHostAndPort.toString());
    }
}