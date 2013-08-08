package com.yammer.breakerbox.service.resources;

import com.yammer.breakerbox.service.views.DashboardView;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/")
public class DashboardResource {
    @GET @Timed @Produces(MediaType.TEXT_HTML)
    public DashboardView render(@QueryParam("cluster") String clusterName) {
        return new DashboardView(clusterName);
    }
}