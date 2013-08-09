package com.yammer.breakerbox.service.resources;

import com.yammer.breakerbox.service.core.ServiceId;
import com.yammer.breakerbox.service.views.ConfigureView;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/configure/{service}")
public class ConfigureResource {
    @GET @Timed @Produces(MediaType.TEXT_HTML)
    public ConfigureView render(@PathParam("service") String serviceName) {
        return new ConfigureView(ServiceId.from(serviceName));
    }
}
