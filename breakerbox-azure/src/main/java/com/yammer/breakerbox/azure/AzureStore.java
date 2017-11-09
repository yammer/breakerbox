package com.yammer.breakerbox.azure;

import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.microsoft.azure.storage.table.TableQuery;
import com.yammer.breakerbox.azure.core.TableId;
import com.yammer.breakerbox.azure.core.TableType;
import com.yammer.breakerbox.azure.healthchecks.TableClientHealthcheck;
import com.yammer.breakerbox.azure.model.DependencyEntity;
import com.yammer.breakerbox.azure.model.DependencyModelByTimestamp;
import com.yammer.breakerbox.azure.model.Entities;
import com.yammer.breakerbox.azure.model.ServiceEntity;
import com.yammer.breakerbox.store.BreakerboxStore;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.store.model.DependencyModel;
import com.yammer.breakerbox.store.model.ServiceModel;
import io.dropwizard.setup.Environment;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Optional;

public class AzureStore extends BreakerboxStore {
    public static final String PARTITION_KEY = "PartitionKey";
    public static final String ROW_KEY = "RowKey";
    protected final TableClient tableClient;

    public AzureStore(AzureTableConfiguration azureTableConfiguration,
                      Environment environment) {
        super(azureTableConfiguration, environment);
        this.tableClient = new TableClientFactory(azureTableConfiguration).create();
        environment.healthChecks().register("azure", new TableClientHealthcheck(tableClient));
    }

    private <T extends TableType> boolean delete(Optional<T> tableType) {
        return !tableType.isPresent() || tableClient.remove(tableType.get());
    }

    @Override
    public boolean initialize() {
        for (TableId tableId : TableId.values()) {
            tableClient.create(tableId);
        }
        return true;
    }

    @Override
    public boolean store(DependencyModel dependencyModel) {
        return tableClient.insertOrReplace(Entities.from(dependencyModel));
    }

    @Override
    public boolean store(ServiceModel serviceModel) {
        return tableClient.insertOrReplace(Entities.from(serviceModel));
    }

    @Override
    public boolean delete(ServiceModel serviceModel) {
        return delete(tableClient.<ServiceEntity>retrieve(Entities.from(serviceModel)));
    }

    @Override
    public boolean delete(DependencyModel dependencyModel) {
        return delete(tableClient.<DependencyEntity>retrieve(Entities.from(dependencyModel)));
    }

    @Override
    public boolean delete(ServiceId serviceId, DependencyId dependencyId) {
        return delete(tableClient.<ServiceEntity>retrieve(ServiceEntity.build(serviceId, dependencyId)));
    }

    @Override
    public boolean delete(DependencyId dependencyId, DateTime dateTime) {
        return delete(fetchByTimestamp(dependencyId, dateTime.getMillis()));
    }

    @Override
    public Optional<ServiceModel> retrieve(ServiceId serviceId, DependencyId dependencyId) {
        return Entities.toServiceModel(tableClient.retrieve(ServiceEntity.build(serviceId, dependencyId)));
    }

    @Override
    public Optional<DependencyModel> retrieve(DependencyId dependencyId, DateTime dateTime) {
        return Entities.toDependencyModel(fetchByTimestamp(dependencyId, dateTime.getMillis()));
    }

    @Override
    public Optional<DependencyModel> retrieveLatest(DependencyId dependencyId, ServiceId serviceId) {
        return fetchLatest(allDependenciesFor(dependencyId, serviceId));
    }

    @Override
    public Collection<ServiceModel> allServiceModels() {
        return Entities.toServiceModelList(allServiceEntities());
    }

    @Override
    public Collection<ServiceModel> listDependenciesFor(final ServiceId serviceId) {
        return Entities.toServiceModelList(allServiceEntities(serviceId));
    }

    @Override
    public Collection<DependencyModel> allDependenciesFor(DependencyId dependencyId, ServiceId serviceId) {
        try (Timer.Context timerContext = dependencyConfigs.time()) {
            return Entities.toDependencyModelList(tableClient.search(
                    TableId.DEPENDENCY, TableQuery
                    .from(DependencyEntity.class)
                    .where(TableQuery.combineFilters(
                            partitionEquals(dependencyId),
                            TableQuery.Operators.AND,
                            serviceIdEquals(serviceId)))));
        }
    }

    private static Optional<DependencyModel> fetchLatest(Iterable<DependencyModel> dependencyModels) {
        if (Iterables.isEmpty(dependencyModels)) {
            return Optional.empty();
        } else {
            return Optional.of(Ordering
                    .from(new DependencyModelByTimestamp())
                    .reverse()
                    .immutableSortedCopy(dependencyModels)
                    .get(0));
        }
    }

    private Optional<DependencyEntity> fetchByTimestamp(DependencyId dependencyId, long timestamp) {
        final ImmutableList<DependencyEntity> dependencyEntities = getConfiguration(dependencyId, timestamp);
        if (dependencyEntities.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(dependencyEntities.get(0));
        }
    }

    private ImmutableList<ServiceEntity> allServiceEntities(ServiceId serviceId) {
        try (Timer.Context timerContext = listService.time()) {
            return tableClient.search(TableId.SERVICE, TableQuery
                    .from(ServiceEntity.class)
                    .where(partitionKeyEquals(serviceId)));
        }
    }

    private static String partitionKeyEquals(ServiceId serviceId) {
        return TableQuery
                .generateFilterCondition(
                        PARTITION_KEY,
                        TableQuery.QueryComparisons.EQUAL,
                        serviceId.getId());
    }

    private ImmutableList<ServiceEntity> allServiceEntities() {
        try (Timer.Context timerContext = listService.time()) {
            return tableClient.search(TableId.SERVICE, TableQuery
                    .from(ServiceEntity.class));
        }
    }

    private ImmutableList<DependencyEntity> getConfiguration(DependencyId dependencyId, long targetTimeStamp) {
        try (Timer.Context timerContext = dependencyConfigs.time()) {
            return tableClient.search(TableId.DEPENDENCY, TableQuery
                    .from(DependencyEntity.class)
                    .where(TableQuery.combineFilters(
                                partitionEquals(dependencyId),
                                TableQuery.Operators.AND,
                                timestampEquals(targetTimeStamp))));
        }
    }

    private static String serviceIdEquals(ServiceId serviceId) {
        return TableQuery.generateFilterCondition(
                "ServiceName",
                TableQuery.QueryComparisons.EQUAL,
                serviceId.getId());
    }

    private static String timestampEquals(long timestamp) {
        return TableQuery.generateFilterCondition(
                ROW_KEY,
                TableQuery.QueryComparisons.EQUAL,
                String.valueOf(timestamp)
        );
    }

    private static String partitionEquals(DependencyId dependencyId) {
        return TableQuery.generateFilterCondition(
                PARTITION_KEY,
                TableQuery.QueryComparisons.EQUAL,
                dependencyId.getId());
    }
}