package com.yammer.breakerbox.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.yammer.breakerbox.service.views.DashboardView;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
public class DashboardResource {
    @GET @Timed @Produces(MediaType.TEXT_HTML)
    public DashboardView render(@DefaultValue("production") @QueryParam("cluster") String clusterName) {
        return new DashboardView(clusterName);
    }
}