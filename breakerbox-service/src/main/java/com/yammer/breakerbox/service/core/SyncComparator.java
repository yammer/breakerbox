package com.yammer.breakerbox.service.core;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.yammer.breakerbox.service.azure.DependencyEntity;
import com.yammer.breakerbox.service.tenacity.TenacityConfigurationFetcher;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SyncComparator {
    private final TenacityConfigurationFetcher.Factory fetcherFactory;
    private final BreakerboxStore breakerboxStore;
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncComparator.class);

    public SyncComparator(TenacityConfigurationFetcher.Factory fetcherFactory, BreakerboxStore breakerboxStore) {
        this.fetcherFactory = fetcherFactory;
        this.breakerboxStore = breakerboxStore;
    }

    private Function<URI, InstanceConfiguration> funFetchConfiguration(final DependencyId dependencyId) {
        return new Function<URI, InstanceConfiguration>() {
            @Override
            public InstanceConfiguration apply(URI input) {
                return new InstanceConfiguration(input, fetcherFactory.create(input, dependencyId).queue());
            }
        };
    }

    private Function<InstanceConfiguration, SyncServiceHostState> funComputeSyncState(final TenacityConfiguration tenacityConfiguration) {
        return new Function<InstanceConfiguration, SyncServiceHostState>() {
            @Override
            public SyncServiceHostState apply(InstanceConfiguration instanceConfiguration) {
                try {
                    if (instanceConfiguration.getTenacityConfiguration().isPresent()) {
                        if (instanceConfiguration.getTenacityConfiguration().get().equals(tenacityConfiguration)) {
                            return SyncServiceHostState.createSynchronized(instanceConfiguration.getUri());
                        } else {
                            return SyncServiceHostState.createUnsynchronized(instanceConfiguration.getUri());
                        }
                    }
                } catch (InterruptedException | ExecutionException err) {
                    LOGGER.warn("Failed to comparing configurations", err);
                }
                return SyncServiceHostState.createUnknown(instanceConfiguration.getUri());
            }
        };
    }

    private Function<InstanceConfiguration, SyncServiceHostState> funUnsynchronized() {
        return new Function<InstanceConfiguration, SyncServiceHostState>() {
            @Override
            public SyncServiceHostState apply(InstanceConfiguration input) {
                return SyncServiceHostState.createUnsynchronized(input.getUri());
            }
        };
    }

    private ImmutableList<InstanceConfiguration> fetch(ServiceId serviceId, DependencyId dependencyId) {
        return FluentIterable
                .from(Instances.propertyKeyUris(serviceId))
                .transform(funFetchConfiguration(dependencyId))
                .toList();
    }

    public ImmutableList<SyncServiceHostState> inSync(ServiceId serviceId, DependencyId dependencyId) {
        final ImmutableList<InstanceConfiguration> configurations = fetch(serviceId, dependencyId);
        final Optional<DependencyEntity> entityOptional = breakerboxStore.retrieveLatest(dependencyId, serviceId);
        if (entityOptional.isPresent()) {

            final DependencyEntity entity = entityOptional.get();
            if (entity.getConfiguration().isPresent()) {
                return FluentIterable
                        .from(configurations)
                        .transform(funComputeSyncState(entity.getConfiguration().get()))
                        .toList();
            } else {
                throw new IllegalStateException("Unable to determine in sync state because of corrupted stored TenacityConfiguration");
            }
        }
        //TODO 08-28-13 cgray: What if services are in sync with each other, but there is no breakerbox configuration?
        return FluentIterable
                .from(configurations)
                .transform(funUnsynchronized())
                .toList();
    }

    public ImmutableList<SyncPropertyKeyState> allInSync(ServiceId serviceId, Iterable<String> propertyKeys) {
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
        private final URI uri;
        private final Future<Optional<TenacityConfiguration>> tenacityConfiguration;

        private InstanceConfiguration(URI uri, Future<Optional<TenacityConfiguration>> tenacityConfiguration) {
            this.uri = uri;
            this.tenacityConfiguration = tenacityConfiguration;
        }

        private URI getUri() {
            return uri;
        }

        private Optional<TenacityConfiguration> getTenacityConfiguration() throws InterruptedException, ExecutionException {
            return tenacityConfiguration.get();
        }
    }
}
