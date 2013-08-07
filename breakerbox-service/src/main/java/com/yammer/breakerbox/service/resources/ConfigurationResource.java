package com.yammer.breakerbox.service.resources;

import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/configuration/{service}/{key}/{environment}")
public class ConfigurationResource {
    @GET @Timed @Produces(MediaType.TEXT_PLAIN)
    public String getConfiguration(@PathParam("service") String service,
                                   @PathParam("key") String key,
                                   @PathParam("environment") String environment) {
        final StringBuilder builder = new StringBuilder();
        builder.append("somekey");
        builder.append('=');
        builder.append("somevalue");
        return builder.toString();
    }
}