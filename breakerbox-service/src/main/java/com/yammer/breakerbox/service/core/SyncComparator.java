package com.yammer.breakerbox.service.core;

import com.google.common.collect.ImmutableList;
import com.netflix.turbine.discovery.Instance;
import com.yammer.breakerbox.service.tenacity.TenacityConfigurationFetcher;
import com.yammer.breakerbox.store.BreakerboxStore;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.store.model.DependencyModel;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SyncComparator {
    private final TenacityConfigurationFetcher.Factory fetcherFactory;
    private final BreakerboxStore breakerboxStore;
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncComparator.class);

    public SyncComparator(TenacityConfigurationFetcher.Factory fetcherFactory, BreakerboxStore breakerboxStore) {
        this.fetcherFactory = fetcherFactory;
        this.breakerboxStore = breakerboxStore;
    }

    private static Function<InstanceConfiguration, SyncServiceHostState> funComputeSyncState(TenacityConfiguration tenacityConfiguration) {
        return (instanceConfiguration) -> {
            try {
                if (instanceConfiguration.getTenacityConfiguration().isPresent()) {
                    if (instanceConfiguration.getTenacityConfiguration().get().equals(tenacityConfiguration)) {
                        return SyncServiceHostState.createSynchronized(Instances.toInstanceId(instanceConfiguration.getInstance()));
                    } else {
                        return SyncServiceHostState.createUnsynchronized(Instances.toInstanceId(instanceConfiguration.getInstance()));
                    }
                }
            } catch (InterruptedException | ExecutionException err) {
                LOGGER.warn("Failed to comparing configurations", err);
            }
            return SyncServiceHostState.createUnknown(Instances.toInstanceId(instanceConfiguration.getInstance()));
        };
    }

    private List<InstanceConfiguration> fetch(ServiceId serviceId, DependencyId dependencyId) {
        return Instances
                .instances(serviceId)
                .stream()
                .map((instance) -> new InstanceConfiguration(instance, fetcherFactory.create(instance, dependencyId).queue()))
                .collect(Collectors.toList());
    }

    public List<SyncServiceHostState> inSync(ServiceId serviceId, DependencyId dependencyId) {
        final List<InstanceConfiguration> configurations = fetch(serviceId, dependencyId);
        
        final Optional<DependencyModel> entityOptional = breakerboxStore.retrieveLatest(dependencyId, serviceId);
        if (entityOptional.isPresent()) {
            final DependencyModel entity = entityOptional.get();
            return configurations
                    .stream()
                    .map(funComputeSyncState(entity.getTenacityConfiguration()))
                    .collect(Collectors.toList());
        }
        return configurations
                .stream()
                .map((instance) -> SyncServiceHostState.createUnsynchronized(Instances.toInstanceId(instance.getInstance())))
                .collect(Collectors.toList());
    }

    public List<SyncPropertyKeyState> allInSync(ServiceId serviceId, Iterable<String> propertyKeys) {
        final ImmutableList.Builder<SyncPropertyKeyState> propertyKeyStateBuilder = ImmutableList.builder();
        for (String propertyKey : propertyKeys) {
            propertyKeyStateBuilder.add(propertyKeySyncStatus(propertyKey, serviceId, DependencyId.from(propertyKey)));
        }
        return propertyKeyStateBuilder.build();
    }

    private SyncPropertyKeyState propertyKeySyncStatus(String propertyKey,
                                                       ServiceId serviceId,
                                                       DependencyId dependencyId) {
        for (SyncServiceHostState syncServiceHostState : inSync(serviceId, dependencyId)) {
            switch (syncServiceHostState.getSyncStatus()) {
                case UNSYNCHRONIZED:
                    return SyncPropertyKeyState.createUnsynchronized(propertyKey);
                case UNKNOWN:
                    return SyncPropertyKeyState.createUnknown(propertyKey);
                case SYNCHRONIZED:
                    break;

            }
        }
        return SyncPropertyKeyState.createSynchronized(propertyKey);
    }

    private static class InstanceConfiguration {
        private final Instance instance;
        private final Future<Optional<TenacityConfiguration>> tenacityConfiguration;

        private InstanceConfiguration(Instance instance, Future<Optional<TenacityConfiguration>> tenacityConfiguration) {
            this.instance = instance;
            this.tenacityConfiguration = tenacityConfiguration;
        }

        public Instance getInstance() {
            return instance;
        }

        private Optional<TenacityConfiguration> getTenacityConfiguration() throws InterruptedException, ExecutionException {
            return tenacityConfiguration.get();
        }

        @Override
        public int hashCode() {
            return Objects.hash(instance, tenacityConfiguration);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final InstanceConfiguration other = (InstanceConfiguration) obj;
            return Objects.equals(this.instance, other.instance)
                    && Objects.equals(this.tenacityConfiguration, other.tenacityConfiguration);
        }
    }
}
