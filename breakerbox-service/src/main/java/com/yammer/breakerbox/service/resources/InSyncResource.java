package com.yammer.breakerbox.service.resources;

import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/sync/{service}/{dependency}")
public class InSyncResource {
    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public void insync(@PathParam("service") String serviceName,
                       @PathParam("dependency") String dependencyName) {

    }
}
