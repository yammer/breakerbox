package com.yammer.breakerbox.service.core;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.microsoft.windowsazure.services.table.client.TableConstants;
import com.microsoft.windowsazure.services.table.client.TableQuery;
import com.yammer.azure.TableClient;
import com.yammer.azure.core.TableType;
import com.yammer.breakerbox.service.azure.DependencyEntity;
import com.yammer.breakerbox.service.azure.ServiceEntity;
import com.yammer.breakerbox.service.azure.TableId;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

public class TenacityStore {
    private final TableClient tableClient;

    private static final Timer LIST_SERVICES = Metrics.newTimer(TenacityStore.class, "list-services");

    public TenacityStore(TableClient tableClient) {
        this.tableClient = tableClient;
    }

    public boolean store(ServiceId serviceId, DependencyId dependencyId) {
        return store(new ServiceEntity(serviceId, dependencyId));
    }

    public boolean store(ServiceEntity serviceEntity) {
        return tableClient.insertOrReplace(serviceEntity);
    }

    public boolean remove(TableType tableType) {
        return tableClient.remove(tableType);
    }

    public Optional<ServiceEntity> retrieve(ServiceId serviceId, DependencyId dependencyId) {
        return tableClient.retrieve(new ServiceEntity(serviceId, dependencyId));
    }

    public Optional<DependencyEntity> retrieve(DependencyId dependencyId, EnvironmentId environmentId) {
        return tableClient.retrieve(DependencyEntity.build(dependencyId, environmentId));
    }

    public boolean store(DependencyId dependencyId, EnvironmentId environmentId) {
        return store(DependencyEntity.build(dependencyId, environmentId));
    }

    public boolean store(DependencyEntity dependencyEntity) {
        return tableClient.insertOrReplace(dependencyEntity);
    }

    public ImmutableList<EnvironmentId> listEnvironments(ServiceEntity serviceEntity) {
        final ImmutableList.Builder<EnvironmentId> builder = ImmutableList.builder();
        for (DependencyEntity entity : tableClient.search(TableQuery
                .from(TableId.DEPENDENCIES.toString(), DependencyEntity.class)
                .where(TableQuery.generateFilterCondition(
                        TableConstants.PARTITION_KEY,
                        TableQuery.QueryComparisons.EQUAL,
                        serviceEntity.getDependencyId().toString())))) {
            builder.add(entity.getEnvironmentId());
        }
        return builder.build();
    }

    public ImmutableList<ServiceEntity> listServices() {
        return allServiceEntities();
    }

    private ImmutableList<ServiceEntity> allServiceEntities() {
        final TimerContext timerContext = LIST_SERVICES.time();
        try {
            return tableClient.search(TableQuery
                    .from(TableId.SERVICES.toString(), ServiceEntity.class));
        } finally {
            timerContext.stop();
        }
    }
}
