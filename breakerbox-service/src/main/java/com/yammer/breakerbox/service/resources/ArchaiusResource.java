package com.yammer.breakerbox.service.resources;

import com.google.common.base.Optional;
import com.yammer.breakerbox.service.archaius.ArchaiusFormatBuilder;
import com.yammer.breakerbox.service.azure.ServiceEntity;
import com.yammer.breakerbox.service.core.BreakerboxStore;
import com.yammer.breakerbox.service.core.ServiceId;
import com.yammer.metrics.annotation.Timed;
import com.yammer.tenacity.core.config.TenacityConfiguration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/archaius/{service}")
public class ArchaiusResource {
    private final ArchaiusResourceV1 delegatingArchaiusResource;

    public ArchaiusResource(ArchaiusResourceV1 delegatingArchaiusResource) {
        this.delegatingArchaiusResource = delegatingArchaiusResource;
    }

    @GET @Timed @Produces(MediaType.TEXT_PLAIN)
    @Deprecated
    public String getServiceConfigurationsDepreciated(@PathParam("service") String service) {
        return delegatingArchaiusResource.getServiceConfigurations(service);
    }
}
