package com.yammer.breakerbox.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.netflix.hystrix.HystrixCommandProperties;
import com.yammer.breakerbox.service.comparable.SortRowFirst;
import com.yammer.breakerbox.service.core.Instances;
import com.yammer.breakerbox.service.core.SyncComparator;
import com.yammer.breakerbox.service.store.TenacityPropertyKeysStore;
import com.yammer.breakerbox.service.views.ConfigureView;
import com.yammer.breakerbox.service.views.NoPropertyKeysView;
import com.yammer.breakerbox.service.views.OptionItem;
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
import io.dropwizard.views.View;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/configure/{service}")
public class ConfigureResource {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigureResource.class);
    private final BreakerboxStore breakerboxStore;
    private final TenacityPropertyKeysStore tenacityPropertyKeysStore;
    private final SyncComparator syncComparator;

    public ConfigureResource(BreakerboxStore breakerboxStore,
                             TenacityPropertyKeysStore tenacityPropertyKeysStore,
                             SyncComparator syncComparator) {
        this.breakerboxStore = breakerboxStore;
        this.tenacityPropertyKeysStore = tenacityPropertyKeysStore;
        this.syncComparator = syncComparator;
    }

    @GET @Timed @Produces(MediaType.TEXT_HTML)
    public View render(@PathParam("service") String serviceName) {
        final ServiceId serviceId = ServiceId.from(serviceName);
        final Optional<String> firstDependencyKey = FluentIterable
                .from(tenacityPropertyKeysStore.tenacityPropertyKeysFor(Instances.propertyKeyUris(serviceId)))
                .first();
        if (firstDependencyKey.isPresent()) {
            return create(serviceId, DependencyId.from(firstDependencyKey.get()), Optional.<Long>absent());
        } else {
            return new NoPropertyKeysView(serviceId);
        }
    }

    @GET @Timed @Produces(MediaType.TEXT_HTML)
    @Path("/{dependency}")
    public ConfigureView render(@PathParam("service") String serviceName,
                                @PathParam("dependency") String dependencyName,
                                @QueryParam("version") String version) {
        return create(ServiceId.from(serviceName), DependencyId.from(dependencyName), getVersion(version));
    }

    private ConfigureView create(ServiceId serviceId,
                                 DependencyId dependencyId,
                                 Optional<Long> version) {
        final Iterable<DependencyModel> dependencyEntities = breakerboxStore.allDependenciesFor(dependencyId, serviceId);
        final ImmutableSet<String> propertyKeys = tenacityPropertyKeysStore.tenacityPropertyKeysFor(Instances.propertyKeyUris(serviceId));
        return new ConfigureView(
                serviceId,
                syncComparator.allInSync(serviceId, propertyKeys),
                getConfiguration(dependencyId, version, serviceId),
                getDependencyVersionNameList(dependencyEntities));
    }

    private TenacityConfiguration getConfiguration(DependencyId dependencyId, Optional<Long> version, ServiceId serviceId) {
        final Optional<DependencyModel> dependencyModel = version.isPresent()
                ? breakerboxStore.retrieve(dependencyId, new DateTime(version.get()))
                : breakerboxStore.retrieveLatest(dependencyId, serviceId);

        if (dependencyModel.isPresent()) {
            return dependencyModel.get().getTenacityConfiguration();
        } else {
            return new TenacityConfiguration();
        }
    }

    private ImmutableList<OptionItem> getDependencyVersionNameList(Iterable<DependencyModel> dependencyModels) {
        final ImmutableList<DependencyModel> sortedEntities =
                Ordering.from(new SortRowFirst())
                        .reverse()
                        .immutableSortedCopy(dependencyModels);

        final ImmutableList.Builder<OptionItem> builder = ImmutableList.builder();
        if (sortedEntities.isEmpty()) {
            builder.add(new OptionItem("Default", 0l));
        } else {
            for (DependencyModel entity : sortedEntities) {
                builder.add(new OptionItem(entity.getDateTime().toString() + " by " + entity.getUser(), entity.getDateTime().getMillis()));
            }
        }
        return builder.build();
    }

    private Optional<Long> getVersion(String version) {
        if (version != null) {
            try {
                return Optional.of(Long.parseLong(version));
            } catch (Exception e) {
                LOG.warn("failed to parse version {}. {}", version, e);
            }
        }
        return Optional.absent();
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
                    .build();
        } else {
            return Response.serverError().build();
        }
    }

}
