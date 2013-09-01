package com.yammer.breakerbox.service.core;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.yammer.breakerbox.service.azure.ServiceEntity;
import com.yammer.breakerbox.service.tenacity.TenacityConfigurationFetcher;
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

    private Function<InstanceConfiguration, SyncState> funComputeSyncState(final TenacityConfiguration tenacityConfiguration) {
        return new Function<InstanceConfiguration, SyncState>() {
            @Override
            public SyncState apply(InstanceConfiguration instanceConfiguration) {
                try {
                    if (instanceConfiguration.getTenacityConfiguration().isPresent()) {
                        if (instanceConfiguration.getTenacityConfiguration().get().equals(tenacityConfiguration)) {
                            return SyncState.createSynchronized(instanceConfiguration.getUri());
                        } else {
                            return SyncState.createUnsynchronized(instanceConfiguration.getUri());
                        }
                    }
                } catch (InterruptedException | ExecutionException err) {
                    LOGGER.warn("Failed to comparing configurations", err);
                }
                return SyncState.createUnknown(instanceConfiguration.getUri());
            }
        };
    }

    private Function<InstanceConfiguration, SyncState> funUnsynchronized() {
        return new Function<InstanceConfiguration, SyncState>() {
            @Override
            public SyncState apply(InstanceConfiguration input) {
                return SyncState.createUnsynchronized(input.getUri());
            }
        };
    }

    private ImmutableList<InstanceConfiguration> fetch(ServiceId serviceId, DependencyId dependencyId) {
        return FluentIterable
                .from(Instances.propertyKeyUris(serviceId))
                .transform(funFetchConfiguration(dependencyId))
                .toList();
    }

    public ImmutableList<SyncState> inSync(ServiceId serviceId, DependencyId dependencyId) {
        final ImmutableList<InstanceConfiguration> configurations = fetch(serviceId, dependencyId);
        final Optional<ServiceEntity> serviceEntity = breakerboxStore.retrieve(serviceId, dependencyId);
        if (serviceEntity.isPresent()) {
            final Optional<TenacityConfiguration> tenacityConfiguration = serviceEntity.get().getTenacityConfiguration();
            if (tenacityConfiguration.isPresent()) {
                return FluentIterable
                        .from(configurations)
                        .transform(funComputeSyncState(tenacityConfiguration.get()))
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