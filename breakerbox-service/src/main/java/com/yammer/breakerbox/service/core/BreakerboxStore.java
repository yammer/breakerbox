package com.yammer.breakerbox.service.core;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.microsoft.windowsazure.services.table.client.TableConstants;
import com.microsoft.windowsazure.services.table.client.TableQuery;
import com.yammer.azure.TableClient;
import com.yammer.azure.core.TableType;
import com.yammer.breakerbox.service.azure.DependencyEntity;
import com.yammer.breakerbox.service.azure.ServiceEntity;
import com.yammer.breakerbox.service.azure.TableId;
import com.yammer.breakerbox.service.util.SimpleDateParser;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BreakerboxStore {
    private final TableClient tableClient;
    private final Cache<ServiceId, ImmutableList<ServiceEntity>> listDependenciesCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private static final Timer LIST_SERVICES = Metrics.newTimer(BreakerboxStore.class, "list-services");
    private static final Timer LIST_SERVICE = Metrics.newTimer(BreakerboxStore.class, "list-service");
    private static final Timer DEPENDENCY_CONFIGS = Metrics.newTimer(BreakerboxStore.class, "latest-dependency-config");
    private static final Logger LOGGER = LoggerFactory.getLogger(BreakerboxStore.class);

    public BreakerboxStore(TableClient tableClient) {
        this.tableClient = tableClient;
    }

    public boolean store(ServiceId serviceId, DependencyId dependencyId, TenacityConfiguration tenacityConfiguration, String username) {
        return store(serviceId, dependencyId)
                &&
                store(dependencyId, System.currentTimeMillis(), tenacityConfiguration, username);
    }

    public boolean store(DependencyId dependencyId, long timestamp, TenacityConfiguration tenacityConfiguration, String username) {
        return tableClient.insertOrReplace(DependencyEntity.build(dependencyId, timestamp, username, tenacityConfiguration));
    }

    public boolean store(ServiceId serviceId, DependencyId dependencyId) {
        ServiceEntity serviceEntity = ServiceEntity.build(serviceId, dependencyId);
        listDependenciesCache.invalidate(serviceEntity.getServiceId());
        return tableClient  .insertOrReplace(serviceEntity);
    }

    public boolean remove(TableType tableType) {
        return tableClient.remove(tableType);
    }

    public Optional<ServiceEntity> retrieve(ServiceId serviceId, DependencyId dependencyId) {
        return tableClient.retrieve(ServiceEntity.build(serviceId, dependencyId));
    }

    public Optional<DependencyEntity> retrieve(DependencyId dependencyId, long timestamp) {
        return tableClient.retrieve(DependencyEntity.build(dependencyId, timestamp));
    }

    //TODO short ttl cache around this
    public Optional<DependencyEntity> retrieve(DependencyId dependencyId, Optional<String> timestamp) {
        final ImmutableList<DependencyEntity> dependencyEntities = listConfigurations(dependencyId);
        if (dependencyEntities.size() == 0) return Optional.absent();
        if (timestamp.isPresent()) {
            return fetchByTimestamp(SimpleDateParser.dateToMillis(timestamp.get()), dependencyEntities);
        } else {
            return Optional.of(fetchLatest(dependencyEntities));
        }
    }

    private DependencyEntity fetchLatest(ImmutableList<DependencyEntity> dependencyEntities) {
        //TODO: This feels clunky - having an off day. Find a smoother way of doing this. FluentIterable?
        final UnmodifiableIterator<DependencyEntity> iterator = dependencyEntities.iterator();
        DependencyEntity latestEntity = iterator.next();
        while (iterator.hasNext()) {
            final DependencyEntity next = iterator.next();
            if (next.getConfigurationTimestamp() > latestEntity.getConfigurationTimestamp()) {
                latestEntity = next;
            }
        }
        return latestEntity;
    }

    private Optional<DependencyEntity> fetchByTimestamp(long timestamp, ImmutableList<DependencyEntity> dependencyEntities) {
        for (DependencyEntity dependencyEntity : dependencyEntities) {
            final DateTime remoteTime = new DateTime(dependencyEntity.getConfigurationTimestamp()).withMillisOfSecond(0);
            final DateTime requestedTime = new DateTime(timestamp).withMillisOfSecond(0);
            if (remoteTime.equals(requestedTime)) {
                return Optional.of(dependencyEntity);
            }
        }
        LOGGER.info("Attempted to fetch dependency {} timestamp {} but wasn't found. Returning default value.", dependencyEntities.get(0).getPartitionKey(), timestamp);
        return Optional.absent();
    }

    public ImmutableList<ServiceEntity> listServices() {
        return allServiceEntities();
    }

    public ImmutableList<ServiceEntity> listDependencies(final ServiceId serviceId) {
        try {
            return listDependenciesCache.get(serviceId, new Callable<ImmutableList<ServiceEntity>>() {
                @Override
                public ImmutableList<ServiceEntity> call() throws Exception {
                    return allServiceEntities(serviceId);
                }
            });
        } catch (ExecutionException err) {
            LOGGER.warn("Could not fetch dependencies for {}", serviceId, err);
        }
        return ImmutableList.of();
    }

    private ImmutableList<ServiceEntity> allServiceEntities(ServiceId serviceId) {
        final TimerContext timerContext = LIST_SERVICE.time();
        try {
            return tableClient.search(TableQuery
                    .from(TableId.SERVICE.toString(), ServiceEntity.class)
                    .where(TableQuery
                            .generateFilterCondition(
                                    TableConstants.PARTITION_KEY,
                                    TableQuery.QueryComparisons.EQUAL,
                                    serviceId.getId())));
        } finally {
            timerContext.stop();
        }
    }

    private ImmutableList<ServiceEntity> allServiceEntities() {
        final TimerContext timerContext = LIST_SERVICES.time();
        try {
            return tableClient.search(TableQuery
                    .from(TableId.SERVICE.toString(), ServiceEntity.class));
        } finally {
            timerContext.stop();
        }
    }

    public ImmutableList<DependencyEntity> listConfigurations(DependencyId dependencyId) {
        final TimerContext timerContext = DEPENDENCY_CONFIGS.time();
        try {
            return tableClient.search(TableQuery
                    .from(TableId.DEPENDENCY.toString(), DependencyEntity.class)
                    .where(TableQuery.generateFilterCondition(
                            TableConstants.PARTITION_KEY,
                            TableQuery.QueryComparisons.EQUAL,
                            dependencyId.getId())));
        } finally {
            timerContext.stop();
        }
    }
}
