package com.yammer.breakerbox.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.netflix.hystrix.HystrixCommandProperties;
import com.yammer.breakerbox.store.BreakerboxStore;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.store.model.DependencyModel;
import com.yammer.breakerbox.store.model.ServiceModel;
import com.yammer.dropwizard.authenticator.User;
import com.yammer.tenacity.core.config.CircuitBreakerConfiguration;
import com.yammer.tenacity.core.config.SemaphoreConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.config.ThreadPoolConfiguration;
import io.dropwizard.auth.Auth;
import org.joda.time.DateTime;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/configure/{service}")
public class ConfigureResource {
    private final BreakerboxStore breakerboxStore;

    public ConfigureResource(BreakerboxStore breakerboxStore) {
        this.breakerboxStore = breakerboxStore;
    }

    @GET @Timed @Produces(MediaType.APPLICATION_JSON)
    @Path("/{dependency}")
    public TenacityConfiguration get(@PathParam("service") String serviceName,
                                     @PathParam("dependency") String dependencyName) {
        final Optional<DependencyModel> entity = breakerboxStore.retrieveLatest(DependencyId.from(dependencyName), ServiceId.from(serviceName));
        if (entity.isPresent()) {
            return entity.get().getTenacityConfiguration();
        }
        throw new WebApplicationException();
    }


    @POST @Timed @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/{dependency}")
    public Response configure(@Auth User user,
                              @PathParam("service") String serviceName,
                              @PathParam("dependency") String dependencyName,
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
                              @FormParam("threadpoolStatisticalWindowBuckets") Integer threadpoolStatisticalWindowBuckets,
                              @FormParam("semaphoreMaxConcurrentRequests") Integer semaphoreMaxConcurrentRequests,
                              @FormParam("semaphoreFallbackMaxConcurrentRequests") Integer semaphoreFallbackMaxConcurrentRequests,
                              @FormParam("executionIsolationStrategy") HystrixCommandProperties.ExecutionIsolationStrategy executionIsolationStrategy) {
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
                new SemaphoreConfiguration(
                        semaphoreMaxConcurrentRequests,
                        semaphoreFallbackMaxConcurrentRequests),
                executionTimeout,
                executionIsolationStrategy);
        final ServiceId serviceId = ServiceId.from(serviceName);
        final DependencyId dependencyId = DependencyId.from(dependencyName);
        if (breakerboxStore.store(new ServiceModel(serviceId, dependencyId)) &&
            breakerboxStore.store(new DependencyModel(dependencyId, DateTime.now(), tenacityConfiguration, user.getName(), serviceId))) {
            return Response
                    .created(URI.create(String.format("/configuration/%s/%s", serviceName, dependencyName)))
                    .entity(tenacityConfiguration)
                    .build();
        } else {
            return Response.serverError().build();
        }
    }

}
