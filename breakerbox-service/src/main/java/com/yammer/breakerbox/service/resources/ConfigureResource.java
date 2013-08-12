package com.yammer.breakerbox.service.resources;

import com.google.common.base.Optional;
import com.yammer.breakerbox.service.azure.ServiceEntity;
import com.yammer.breakerbox.service.core.DependencyId;
import com.yammer.breakerbox.service.core.Instances;
import com.yammer.breakerbox.service.core.ServiceId;
import com.yammer.breakerbox.service.core.TenacityStore;
import com.yammer.breakerbox.service.store.TenacityPropertyKeysStore;
import com.yammer.breakerbox.service.views.ConfigureView;
import com.yammer.metrics.annotation.Timed;
import com.yammer.tenacity.core.config.CircuitBreakerConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.config.ThreadPoolConfiguration;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/configure/{service}")
public class ConfigureResource {
    private final TenacityStore tenacityStore;
    private final TenacityPropertyKeysStore tenacityPropertyKeysStore;

    public ConfigureResource(TenacityStore tenacityStore, TenacityPropertyKeysStore tenacityPropertyKeysStore) {
        this.tenacityStore = tenacityStore;
        this.tenacityPropertyKeysStore = tenacityPropertyKeysStore;
    }

    @GET @Timed @Produces(MediaType.TEXT_HTML)
    public ConfigureView render(@PathParam("service") String serviceName) {
        final ServiceId serviceId = ServiceId.from(serviceName);
        return new ConfigureView(
                serviceId,
                tenacityPropertyKeysStore.tenacityPropertyKeysFor(Instances.propertyKeyUris(serviceId)),
                new TenacityConfiguration());
    }

    @GET @Timed @Produces(MediaType.APPLICATION_JSON)
    @Path("/{dependency}")
    public TenacityConfiguration get(@PathParam("service") String serviceName,
                                     @PathParam("dependency") String dependencyName) {
        final Optional<ServiceEntity> serviceEntity = tenacityStore.retrieve(
                ServiceId.from(serviceName),
                DependencyId.from(dependencyName));
        if (serviceEntity.isPresent()) {
            final Optional<TenacityConfiguration> tenacityConfiguration = serviceEntity.get().getTenacityConfiguration();
            if (tenacityConfiguration.isPresent()) {
                return tenacityConfiguration.get();
            }
        }
        throw new WebApplicationException();
    }


    @POST @Timed @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response configure(@PathParam("service") String serviceName,
                              @FormParam("dependency") String dependencyName,
                              @FormParam("executionTimeout") Integer executionTimeout,
                              @FormParam("requestVolumeThreshold") Integer requestVolumeThreshold,
                              @FormParam("errorThresholdPercentage") Integer errorThresholdPercentage,
                              @FormParam("sleepWindow") Integer sleepWindow,
                              @FormParam("circuitBreakerstatisticalWindow") Integer circuitBreakerstatisticalWindow,
                              @FormParam("circuitBreakerStatisticalWindowBuckets") Integer circuitBreakerStatisticalWindowBuckets,
                              @FormParam("threadPoolCoreSize") Integer threadPoolCoreSize,
                              @FormParam("keepAliveMinutes") Integer keepAliveMinutes,
                              @FormParam("maxQueueSize") Integer maxQueueSize,
                              @FormParam("queueSizeRejectionThreshold") Integer queueSizeRejectionThreshold,
                              @FormParam("threadpoolStatisticalWindow") Integer threadpoolStatisticalWindow,
                              @FormParam("threadpoolStatisticalWindowBuckets") Integer threadpoolStatisticalWindowBuckets) {
        final TenacityConfiguration tenacityConfiguration = new TenacityConfiguration(
                new ThreadPoolConfiguration(
                        threadPoolCoreSize,
                        keepAliveMinutes,
                        maxQueueSize,
                        queueSizeRejectionThreshold,
                        threadpoolStatisticalWindow,
                        threadpoolStatisticalWindowBuckets),
                new CircuitBreakerConfiguration(
                        requestVolumeThreshold,
                        sleepWindow,
                        errorThresholdPercentage,
                        circuitBreakerstatisticalWindow,
                        circuitBreakerStatisticalWindowBuckets),
                executionTimeout);
        if (tenacityStore.store(
                ServiceId.from(serviceName),
                DependencyId.from(dependencyName),
                tenacityConfiguration)) {
            return Response
                    .created(URI.create(String.format("/configuration/%s/%s", serviceName, dependencyName)))
                    .build();
        } else {
            return Response.serverError().build();
        }
    }
}
