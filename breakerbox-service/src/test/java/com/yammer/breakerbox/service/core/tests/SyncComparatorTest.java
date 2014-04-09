package com.yammer.breakerbox.service.core.tests;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.yammer.breakerbox.azure.model.DependencyEntity;
import com.yammer.breakerbox.service.core.Instances;
import com.yammer.breakerbox.service.core.SyncComparator;
import com.yammer.breakerbox.service.core.SyncServiceHostState;
import com.yammer.breakerbox.service.tenacity.TenacityConfigurationFetcher;
import com.yammer.breakerbox.store.BreakerboxStore;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.store.model.DependencyModel;
import com.yammer.breakerbox.store.model.ServiceModel;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SyncComparatorTest {
    TenacityConfigurationFetcher.Factory mockFactory;
    BreakerboxStore mockTenacityStory;
    TenacityConfigurationFetcher mockFetcher;
    ServiceId serviceId = ServiceId.from("production");
    DependencyId dependencyId = DependencyId.from(UUID.randomUUID().toString());
    private long testTimestamp = System.currentTimeMillis() - 1000;

    @Before
    public void setup() {
        mockFactory = mock(TenacityConfigurationFetcher.Factory.class);
        mockTenacityStory = mock(BreakerboxStore.class);
        mockFetcher = mock(TenacityConfigurationFetcher.class);
    }

    private ImmutableList<SyncServiceHostState> unsynchronized() {
        return FluentIterable
                .from(Instances.propertyKeyUris(serviceId))
                .transform(new Function<URI, SyncServiceHostState>() {
                    @Override
                    public SyncServiceHostState apply(URI input) {
                        return SyncServiceHostState.createUnsynchronized(input);
                    }
                })
                .toList();
    }

    private ImmutableList<SyncServiceHostState> allSynchronized() {
        return FluentIterable
                .from(Instances.propertyKeyUris(serviceId))
                .transform(new Function<URI, SyncServiceHostState>() {
                    @Override
                    public SyncServiceHostState apply(URI input) {
                        return SyncServiceHostState.createSynchronized(input);
                    }
                })
                .toList();
    }

    private ImmutableList<SyncServiceHostState> unknown() {
        return FluentIterable
                .from(Instances.propertyKeyUris(serviceId))
                .transform(new Function<URI, SyncServiceHostState>() {
                    @Override
                    public SyncServiceHostState apply(URI input) {
                        return SyncServiceHostState.createUnknown(input);
                    }
                })
                .toList();
    }

    private ImmutableList<SyncServiceHostState> allSynchronizedExceptUnknown(final URI uri) {
        return FluentIterable
                .from(Instances.propertyKeyUris(serviceId))
                .transform(new Function<URI, SyncServiceHostState>() {
                    @Override
                    public SyncServiceHostState apply(URI input) {
                        if (input.equals(uri)) {
                            return SyncServiceHostState.createUnknown(input);
                        } else {
                            return SyncServiceHostState.createSynchronized(input);
                        }
                    }
                })
                .toList();
    }

    private ImmutableList<SyncServiceHostState> allSynchronizedExcept(final URI uri) {
        return FluentIterable
                .from(Instances.propertyKeyUris(serviceId))
                .transform(new Function<URI, SyncServiceHostState>() {
                    @Override
                    public SyncServiceHostState apply(URI input) {
                        if (input.equals(uri)) {
                            return SyncServiceHostState.createUnsynchronized(input);
                        } else {
                            return SyncServiceHostState.createSynchronized(input);
                        }
                    }
                })
                .toList();
    }

    @Test
     public void noBreakerboxConfiguration() {
        final SyncComparator syncComparator = new SyncComparator(mockFactory, mockTenacityStory);

        when(mockTenacityStory.retrieveLatest(dependencyId, serviceId)).thenReturn(Optional.<DependencyModel>absent());
        when(mockFactory.create(any(URI.class), any(TenacityPropertyKey.class))).thenReturn(mockFetcher);
        when(mockFetcher.queue()).thenReturn(Futures.immediateFuture(Optional.of(new TenacityConfiguration())));

        assertThat(syncComparator.inSync(serviceId, dependencyId))
                .isEqualTo(unsynchronized());
    }

    @Test
    public void noConfigurations() {
        final SyncComparator syncComparator = new SyncComparator(mockFactory, mockTenacityStory);

        when(mockTenacityStory.retrieveLatest(dependencyId, serviceId)).thenReturn(Optional.<DependencyModel>absent());
        when(mockFactory.create(any(URI.class), any(TenacityPropertyKey.class))).thenReturn(mockFetcher);
        when(mockFetcher.queue()).thenReturn(Futures.immediateFuture(Optional.<TenacityConfiguration>absent()));

        assertThat(syncComparator.inSync(serviceId, dependencyId))
                .isEqualTo(unsynchronized());
    }

    @Test
    public void differentBreakerboxConfiguration() {
        final SyncComparator syncComparator = new SyncComparator(mockFactory, mockTenacityStory);
        final TenacityConfiguration differentConfiguration = new TenacityConfiguration();
        differentConfiguration.setExecutionIsolationThreadTimeoutInMillis(9);

        when(mockTenacityStory.retrieve(serviceId, dependencyId))
                .thenReturn(Optional.of(new ServiceModel(serviceId, dependencyId)));
        when(mockTenacityStory.retrieveLatest(dependencyId, serviceId))
                .thenReturn(Optional.of(new DependencyModel(dependencyId, new DateTime(testTimestamp), differentConfiguration, "fooUser", serviceId)));
        when(mockFactory.create(any(URI.class), any(TenacityPropertyKey.class))).thenReturn(mockFetcher);
        when(mockFetcher.queue()).thenReturn(Futures.immediateFuture(Optional.of(new TenacityConfiguration())));

        assertThat(syncComparator.inSync(serviceId, dependencyId))
                .isEqualTo(unsynchronized());
    }

    @Test
    public void sameBreakerboxConfiguration() {
        final SyncComparator syncComparator = new SyncComparator(mockFactory, mockTenacityStory);

        when(mockTenacityStory.retrieve(serviceId, dependencyId))
                .thenReturn(Optional.of(new ServiceModel(serviceId, dependencyId)));
        when(mockTenacityStory.retrieveLatest(dependencyId, serviceId))
                .thenReturn(Optional.of(new DependencyModel(dependencyId, new DateTime(testTimestamp), DependencyEntity.defaultConfiguration(), "fooUser", serviceId)));
        when(mockFactory.create(any(URI.class), any(TenacityPropertyKey.class))).thenReturn(mockFetcher);
        when(mockFetcher.queue()).thenReturn(Futures.immediateFuture(Optional.of(new TenacityConfiguration())));

        assertThat(syncComparator.inSync(serviceId, dependencyId))
                .isEqualTo(allSynchronized());
    }

    @Test
    public void oneIsDifferent() {
        final SyncComparator syncComparator = new SyncComparator(mockFactory, mockTenacityStory);
        final TenacityConfigurationFetcher differentFetcher = mock(TenacityConfigurationFetcher.class);
        final TenacityConfiguration differentConfiguration = new TenacityConfiguration();
        differentConfiguration.setExecutionIsolationThreadTimeoutInMillis(9);
        final URI differentURI = URI.create("http://deploy-001.sjc1.yammer.com:9090");

        when(mockTenacityStory.retrieve(serviceId, dependencyId))
                .thenReturn(Optional.of(new ServiceModel(serviceId, dependencyId)));
        when(mockTenacityStory.retrieveLatest(dependencyId, serviceId))
                .thenReturn(Optional.of(new DependencyModel(dependencyId, new DateTime(testTimestamp), DependencyEntity.defaultConfiguration(), "fooUser", serviceId)));
        when(mockFactory.create(any(URI.class), any(TenacityPropertyKey.class))).thenReturn(mockFetcher);
        when(mockFactory.create(eq(differentURI), any(TenacityPropertyKey.class)))
                .thenReturn(differentFetcher);
        when(mockFetcher.queue()).thenReturn(Futures.immediateFuture(Optional.of(new TenacityConfiguration())));
        when(differentFetcher.queue()).thenReturn(Futures.immediateFuture(Optional.of(differentConfiguration)));

        assertThat(syncComparator.inSync(serviceId, dependencyId))
                .isEqualTo(allSynchronizedExcept(differentURI));
    }

    @Test
    public void oneFailedFetching() {
        final SyncComparator syncComparator = new SyncComparator(mockFactory, mockTenacityStory);
        final TenacityConfigurationFetcher differentFetcher = mock(TenacityConfigurationFetcher.class);
        final TenacityConfiguration differentConfiguration = new TenacityConfiguration();
        differentConfiguration.setExecutionIsolationThreadTimeoutInMillis(9);
        final URI differentURI = URI.create("http://deploy-001.sjc1.yammer.com:9090");

        when(mockTenacityStory.retrieve(serviceId, dependencyId))
                .thenReturn(Optional.of(new ServiceModel(serviceId, dependencyId)));
        when(mockTenacityStory.retrieveLatest(dependencyId, serviceId))
                .thenReturn(Optional.of(new DependencyModel(dependencyId, new DateTime(testTimestamp), DependencyEntity.defaultConfiguration(), "fooUser", serviceId)));
        when(mockFactory.create(any(URI.class), any(TenacityPropertyKey.class))).thenReturn(mockFetcher);
        when(mockFactory.create(eq(differentURI), any(TenacityPropertyKey.class)))
                .thenReturn(differentFetcher);
        when(mockFetcher.queue()).thenReturn(Futures.immediateFuture(Optional.of(new TenacityConfiguration())));
        when(differentFetcher.queue()).thenReturn(Futures.immediateFuture(Optional.<TenacityConfiguration>absent()));

        assertThat(syncComparator.inSync(serviceId, dependencyId))
                .isEqualTo(allSynchronizedExceptUnknown(differentURI));
    }

    @Test
    public void failedToFetchConfigurationsFromServices() {
        final SyncComparator syncComparator = new SyncComparator(mockFactory, mockTenacityStory);

        when(mockTenacityStory.retrieve(serviceId, dependencyId))
                .thenReturn(Optional.of(new ServiceModel(serviceId, dependencyId)));
        when(mockTenacityStory.retrieveLatest(dependencyId, serviceId))
                .thenReturn(Optional.of(new DependencyModel(dependencyId, new DateTime(testTimestamp), DependencyEntity.defaultConfiguration(), "fooUser", serviceId)));
        when(mockFactory.create(any(URI.class), any(TenacityPropertyKey.class))).thenReturn(mockFetcher);
        when(mockFetcher.queue()).thenReturn(Futures.immediateFuture(Optional.<TenacityConfiguration>absent()));

        assertThat(syncComparator.inSync(serviceId, dependencyId))
                .isEqualTo(unknown());
    }

    @Test
    public void testKeyRunningOnDefaultConfig() throws Exception {
        final SyncComparator syncComparator = new SyncComparator(mockFactory, mockTenacityStory);

        when(mockTenacityStory.retrieveLatest(dependencyId, serviceId))
                .thenReturn(Optional.<DependencyModel>absent());
        when(mockFactory.create(any(URI.class), any(TenacityPropertyKey.class))).thenReturn(mockFetcher);
        when(mockFetcher.queue()).thenReturn(Futures.immediateFuture(Optional.<TenacityConfiguration>absent()));

        assertThat(syncComparator.inSync(serviceId,dependencyId))
                .isEqualTo(unsynchronized());


    }
}
