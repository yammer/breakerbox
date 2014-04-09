package com.yammer.breakerbox.azure;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.microsoft.windowsazure.services.table.client.TableConstants;
import com.microsoft.windowsazure.services.table.client.TableQuery;
import com.yammer.breakerbox.azure.core.TableId;
import com.yammer.breakerbox.azure.core.TableType;
import com.yammer.breakerbox.azure.model.DependencyEntity;
import com.yammer.breakerbox.azure.model.DependencyModelByTimestamp;
import com.yammer.breakerbox.azure.model.ServiceEntity;
import com.yammer.breakerbox.store.BreakerboxStore;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.store.model.DependencyModel;
import com.yammer.breakerbox.store.model.ServiceModel;
import com.yammer.metrics.core.TimerContext;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

public class AzureStore extends BreakerboxStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStore.class);
    private final TableClient tableClient;

    public AzureStore(TableClient tableClient) {
        this.tableClient = checkNotNull(tableClient);
    }

    private static ServiceEntity from(ServiceModel serviceModel) {
        return ServiceEntity.build(serviceModel.getServiceId(), serviceModel.getDependencyId());
    }

    private static ImmutableList<ServiceModel> toServiceModelList(Iterable<ServiceEntity> serviceEntities) {
        return FluentIterable
                .from(serviceEntities)
                .transform(new Function<ServiceEntity, ServiceModel>() {
                    @Nullable
                    @Override
                    public ServiceModel apply(@Nullable ServiceEntity input) {
                        return to(input);
                    }
                })
                .toList();
    }

    private static Optional<ServiceModel> toServiceModel(Optional<ServiceEntity> serviceEntity) {
        if (serviceEntity.isPresent()) {
            return Optional.of(to(serviceEntity.get()));
        }
        return Optional.absent();
    }

    private static ServiceModel to(ServiceEntity serviceEntity) {
        return new ServiceModel(serviceEntity.getServiceId(), serviceEntity.getDependencyId());
    }

    private static DependencyEntity from(DependencyModel dependencyModel) {
        return DependencyEntity.build(
                dependencyModel.getDependencyId(),
                dependencyModel.getUser(),
                dependencyModel.getTenacityConfiguration(),
                dependencyModel.getServiceId());
    }

    private static ImmutableList<DependencyModel> toDependencyModelList(Iterable<DependencyEntity> dependencyEntities) {
        return FluentIterable
                .from(dependencyEntities)
                .transform(new Function<DependencyEntity, DependencyModel>() {
                    @Nullable
                    @Override
                    public DependencyModel apply(@Nullable DependencyEntity input) {
                        return to(input);
                    }
                })
                .toList();
    }

    private static Optional<DependencyModel> toDependencyModel(Optional<DependencyEntity> dependencyEntity) {
        if (dependencyEntity.isPresent()) {
            return Optional.of(to(dependencyEntity.get()));
        }
        return Optional.absent();
    }

    private static DependencyModel to(DependencyEntity dependencyEntity) {
        return new DependencyModel(
                dependencyEntity.getDependencyId(),
                new DateTime(dependencyEntity.getConfigurationTimestamp()),
                dependencyEntity.getConfiguration().get(),
                dependencyEntity.getUser(),
                dependencyEntity.getServiceId());
    }

    private <T extends TableType> boolean delete(Optional<T> tableType) {
        return !tableType.isPresent() || tableClient.remove(tableType.get());
    }

    @Override
    public boolean store(DependencyModel dependencyModel) {
        return tableClient.insertOrReplace(from(dependencyModel));
    }

    @Override
    public boolean store(ServiceModel serviceModel) {
        return tableClient.insertOrReplace(from(serviceModel));
    }

    @Override
    public boolean delete(ServiceModel serviceModel) {
        return delete(tableClient.<ServiceEntity>retrieve(from(serviceModel)));
    }

    @Override
    public boolean delete(DependencyModel dependencyModel) {
        return delete(tableClient.<DependencyEntity>retrieve(from(dependencyModel)));
    }

    @Override
    public boolean delete(ServiceId serviceId, DependencyId dependencyId) {
        return delete(tableClient.<ServiceEntity>retrieve(ServiceEntity.build(serviceId, dependencyId)));
    }

    @Override
    public boolean delete(DependencyId dependencyId, DateTime dateTime, ServiceId serviceId) {
        return delete(fetchByTimestamp(dependencyId, dateTime.getMillis(), serviceId));
    }

    @Override
    public Optional<ServiceModel> retrieve(ServiceId serviceId, DependencyId dependencyId) {
        return toServiceModel(tableClient.<ServiceEntity>retrieve(ServiceEntity.build(serviceId, dependencyId)));
    }

    @Override
    public Optional<DependencyModel> retrieve(DependencyId dependencyId, DateTime dateTime, ServiceId serviceId) {
        return toDependencyModel(fetchByTimestamp(dependencyId, dateTime.getMillis(), serviceId));
    }

    @Override
    public Optional<DependencyModel> retrieveLatest(DependencyId dependencyId, ServiceId serviceId) {
        return fetchLatest(allDependenciesFor(dependencyId, serviceId));
    }

    @Override
    public Iterable<ServiceModel> allServiceModels() {
        return toServiceModelList(allServiceEntities());
    }

    @Override
    public Iterable<ServiceModel> listDependenciesFor(final ServiceId serviceId) {
        try {
            return listDependenciesCache.get(serviceId, new Callable<ImmutableList<ServiceModel>>() {
                @Override
                public ImmutableList<ServiceModel> call() throws Exception {
                    return toServiceModelList(allServiceEntities(serviceId));
                }
            });
        } catch (ExecutionException err) {
            LOGGER.warn("Could not fetch dependencies for {}", serviceId, err);
        }
        return ImmutableList.of();
    }

    @Override
    public Iterable<DependencyModel> allDependenciesFor(DependencyId dependencyId, ServiceId serviceId) {
        final TimerContext timerContext = DEPENDENCY_CONFIGS.time();
        try {
            return toDependencyModelList(tableClient.search(TableQuery
                    .from(TableId.DEPENDENCY.toString(), DependencyEntity.class)
                    .where(TableQuery.combineFilters(
                            partitionEquals(dependencyId),
                            TableQuery.Operators.AND,
                            serviceIdEquals(serviceId)))));
        } finally {
            timerContext.stop();
        }
    }

    private static Optional<DependencyModel> fetchLatest(Iterable<DependencyModel> dependencyModels) {
        if (Iterables.isEmpty(dependencyModels)) {
            return Optional.absent();
        } else {
            return Optional.of(Ordering
                    .from(new DependencyModelByTimestamp())
                    .reverse()
                    .immutableSortedCopy(dependencyModels)
                    .get(0));
        }
    }

    private Optional<DependencyEntity> fetchByTimestamp(DependencyId dependencyId, long timestamp, ServiceId serviceId) {
        final ImmutableList<DependencyEntity> dependencyEntities = getConfiguration(dependencyId, timestamp, serviceId);
        if (dependencyEntities.isEmpty()) {
            return Optional.absent();
        } else {
            return Optional.of(dependencyEntities.get(0));
        }
    }

    private ImmutableList<ServiceEntity> allServiceEntities(ServiceId serviceId) {
        final TimerContext timerContext = LIST_SERVICE.time();
        try {
            return tableClient.search(TableQuery
                    .from(TableId.SERVICE.toString(), ServiceEntity.class)
                    .where(partitionKeyEquals(serviceId)));
        } finally {
            timerContext.stop();
        }
    }

    private static String partitionKeyEquals(ServiceId serviceId) {
        return TableQuery
                .generateFilterCondition(
                        TableConstants.PARTITION_KEY,
                        TableQuery.QueryComparisons.EQUAL,
                        serviceId.getId());
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

    private ImmutableList<DependencyEntity> getConfiguration(DependencyId dependencyId, long targetTimeStamp, ServiceId serviceId) {
        final TimerContext timerContext = DEPENDENCY_CONFIGS.time();
        try {
            return tableClient.search(TableQuery
                    .from(TableId.DEPENDENCY.toString(), DependencyEntity.class)
                    .where(TableQuery.combineFilters(
                            TableQuery.combineFilters(
                                    partitionEquals(dependencyId),
                                    TableQuery.Operators.AND,
                                    timestampEquals(targetTimeStamp)),
                            TableQuery.Operators.AND,
                            serviceIdEquals(serviceId))));
        } finally {
            timerContext.stop();
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
                TableConstants.ROW_KEY,
                TableQuery.QueryComparisons.EQUAL,
                String.valueOf(timestamp)
        );
    }

    private static String partitionEquals(DependencyId dependencyId) {
        return TableQuery.generateFilterCondition(
                TableConstants.PARTITION_KEY,
                TableQuery.QueryComparisons.EQUAL,
                dependencyId.getId());
    }
}
