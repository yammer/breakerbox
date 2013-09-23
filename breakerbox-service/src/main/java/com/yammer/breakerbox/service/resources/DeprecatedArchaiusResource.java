package com.yammer.breakerbox.service.resources;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.yammer.breakerbox.service.archaius.ArchaiusFormatBuilder;
import com.yammer.breakerbox.service.azure.DependencyEntity;
import com.yammer.breakerbox.service.azure.ServiceEntity;
import com.yammer.breakerbox.service.core.BreakerboxStore;
import com.yammer.breakerbox.service.core.ServiceId;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

//TODO: remove after legacy services no longer are pointing at this resource
@Deprecated
@Path("/v1/archaius/{service}")
public class DeprecatedArchaiusResource {
    private final BreakerboxStore breakerboxStore;

    public DeprecatedArchaiusResource(BreakerboxStore breakerboxStore) {
        this.breakerboxStore = breakerboxStore;
    }

    @GET
    @Timed
    @Produces(MediaType.TEXT_PLAIN)
    public String getServiceConfigurations(@PathParam("service") String service) {
        final ArchaiusFormatBuilder archaiusBuilder = ArchaiusFormatBuilder.builder();
        final ServiceId serviceId = ServiceId.from(service);
        final ImmutableList<ServiceEntity> propertyKeys = breakerboxStore.listDependencies(serviceId);
        for (ServiceEntity propertyKey : propertyKeys) {
            final Optional<DependencyEntity> dependencyEntity = breakerboxStore.retrieveLatest(propertyKey.getDependencyId(), serviceId);
            if (dependencyEntity.isPresent()) {
                archaiusBuilder.with(propertyKey.getDependencyId(), dependencyEntity.get().getConfiguration().or(DependencyEntity.defaultConfiguration()));
            }
        }
        archaiusBuilder.hystrixMetricsStreamServletMaxConnections(10);
        return archaiusBuilder.build();
    }
}
