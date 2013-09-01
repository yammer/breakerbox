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
    private final BreakerboxStore breakerboxStore;

    public ArchaiusResource(BreakerboxStore breakerboxStore) {
        this.breakerboxStore = breakerboxStore;
    }

    @GET @Timed @Produces(MediaType.TEXT_PLAIN)
    public String getServiceConfigurations(@PathParam("service") String service) {
        final ArchaiusFormatBuilder archaiusBuilder = ArchaiusFormatBuilder.builder();
        for (ServiceEntity serviceEntity : breakerboxStore.listDependencies(ServiceId.from(service))) {
            final Optional<TenacityConfiguration> tenacityConfiguration = serviceEntity.getTenacityConfiguration();
            if (tenacityConfiguration.isPresent()) {
                archaiusBuilder
                        .with(serviceEntity.getDependencyId(), tenacityConfiguration.get());
            }
        }
        return archaiusBuilder.build();
    }
}