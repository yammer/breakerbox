package com.yammer.breakerbox.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.yammer.breakerbox.service.views.DashboardView;
import com.yammer.breakerbox.service.views.DashboardViewFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
public class DashboardResource {
    private final DashboardViewFactory viewFactory;

    public DashboardResource(DashboardViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    @GET @Timed @Produces(MediaType.TEXT_HTML)
    public DashboardView render(@DefaultValue("production") @QueryParam("cluster") String clusterName) {
        return viewFactory.create(clusterName);
    }
}