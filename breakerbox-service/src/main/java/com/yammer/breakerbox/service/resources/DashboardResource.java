package com.yammer.breakerbox.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.yammer.breakerbox.service.views.DashboardView;
import com.yammer.breakerbox.service.views.DashboardViewFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/")
public class DashboardResource {
    private final String defaultDashboard;
    private final DashboardViewFactory viewFactory;

    public DashboardResource(DashboardViewFactory viewFactory, String defaultDashboard) {
        this.viewFactory = viewFactory;
        this.defaultDashboard = defaultDashboard;
    }

    @GET @Timed @Produces(MediaType.TEXT_HTML)
    public DashboardView render(@QueryParam("cluster") Optional<String> clusterName) {
        return viewFactory.create(clusterName.or(defaultDashboard));
    }
}