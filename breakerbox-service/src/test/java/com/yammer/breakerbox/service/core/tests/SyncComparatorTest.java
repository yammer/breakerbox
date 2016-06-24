package com.yammer.breakerbox.service.core.tests;

import com.google.common.base.Optional;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.Futures;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.plugins.PluginsFactory;
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
import com.yammer.breakerbox.turbine.YamlInstanceDiscovery;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.testing.TenacityTestRule;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SyncComparatorTest {
    @Rule
    public final TenacityTestRule tenacityTestRule = new TenacityTestRule();
    TenacityConfigurationFetcher.Factory mockFactory;
    BreakerboxStore mockTenacityStory;
    TenacityConfigurationFetcher mockFetcher;
    ServiceId serviceId = ServiceId.from("production");
    DependencyId dependencyId = DependencyId.from(UUID.randomUUID().toString());
    private long testTimestamp = System.currentTimeMillis() - 1000;

    @Before
    public void setup() throws Exception {
        PluginsFactory.setInstanceDiscovery(new YamlInstanceDiscovery(
                Paths.get(Resources.getResource("turbineConfigurations/instances.yml").toURI()),
                Validators.newValidator(),
                Jackson.newObjectMapper()));
        mockFactory = mock(TenacityConfigurationFetcher.Factory.class);
        mockTenacityStory = mock(BreakerboxStore.class);
        mockFetcher = mock(TenacityConfigurationFetcher.class);
    }

    private List<SyncServiceHostState> unsynchronized() {
        return Instances
                .instances(serviceId)
                .stream()
                .map((instance) -> SyncServiceHostState.createUnsynchronized(Instances.toInstanceId(instance)))
                .collect(Collectors.toList());
    }

    private List<SyncServiceHostState> allSynchronized() {
        return Instances
                .instances(serviceId)
                .stream()
                .map((instance) -> SyncServiceHostState.createSynchronized(Instances.toInstanceId(instance)))
                .collect(Collectors.toList());
    }

    private List<SyncServiceHostState> unknown() {
        return Instances
                .instances(serviceId)
                .stream()
                .map((instance) -> SyncServiceHostState.createUnknown(Instances.toInstanceId(instance)))
                .collect(Collectors.toList());
    }

    private List<SyncServiceHostState> allSynchronizedExceptUnknown(Instance exceptionInstance) {
        return Instances
                .instances(serviceId)
                .stream()
                .map((instance) -> {
                    if (instance.equals(exceptionInstance)) {
                        return SyncServiceHostState.createUnknown(Instances.toInstanceId(instance));
                    } else {
                        return SyncServiceHostState.createSynchronized(Instances.toInstanceId(instance));
                    }
                })
                .collect(Collectors.toList());
    }

    private List<SyncServiceHostState> allSynchronizedExcept(Instance exceptionInstance) {
        return Instances
                .instances(serviceId)
                .stream()
                .map((instance) -> {
                    if (instance.equals(exceptionInstance)) {
                        return SyncServiceHostState.createUnsynchronized(Instances.toInstanceId(instance));
                    } else {
                        return SyncServiceHostState.createSynchronized(Instances.toInstanceId(instance));
                    }
                })
                .collect(Collectors.toList());
    }

    @Test
     public void noBreakerboxConfiguration() {
        final SyncComparator syncComparator = new SyncComparator(mockFactory, mockTenacityStory);

        when(mockTenacityStory.retrieveLatest(dependencyId, serviceId)).thenReturn(Optional.<DependencyModel>absent());
        when(mockFactory.create(any(Instance.class), any(TenacityPropertyKey.class))).thenReturn(mockFetcher);
        when(mockFetcher.queue()).thenReturn(Futures.immediateFuture(Optional.of(new TenacityConfiguration())));

        assertThat(syncComparator.inSync(serviceId, dependencyId))
                .isEqualTo(unsynchronized());
    }

    @Test
    public void noConfigurations() {
        final SyncComparator syncComparator = new SyncComparator(mockFactory, mockTenacityStory);

        when(mockTenacityStory.retrieveLatest(dependencyId, serviceId)).thenReturn(Optional.<DependencyModel>absent());
        when(mockFactory.create(any(Instance.class), any(TenacityPropertyKey.class))).thenReturn(mockFetcher);
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
        when(mockFactory.create(any(Instance.class), any(TenacityPropertyKey.class))).thenReturn(mockFetcher);
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
        when(mockFactory.create(any(Instance.class), any(TenacityPropertyKey.class))).thenReturn(mockFetcher);
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
        final Instance differentInstance = new Instance("http://deploy-001.sjc1.yammer.com:9090", "ignored", true);

        when(mockTenacityStory.retrieve(serviceId, dependencyId))
                .thenReturn(Optional.of(new ServiceModel(serviceId, dependencyId)));
        when(mockTenacityStory.retrieveLatest(dependencyId, serviceId))
                .thenReturn(Optional.of(new DependencyModel(dependencyId, new DateTime(testTimestamp), DependencyEntity.defaultConfiguration(), "fooUser", serviceId)));
        when(mockFactory.create(any(Instance.class), any(TenacityPropertyKey.class))).thenReturn(mockFetcher);
        when(mockFactory.create(eq(differentInstance), any(TenacityPropertyKey.class)))
                .thenReturn(differentFetcher);
        when(mockFetcher.queue()).thenReturn(Futures.immediateFuture(Optional.of(new TenacityConfiguration())));
        when(differentFetcher.queue()).thenReturn(Futures.immediateFuture(Optional.of(differentConfiguration)));

        assertThat(syncComparator.inSync(serviceId, dependencyId))
                .isEqualTo(allSynchronizedExcept(differentInstance));
    }

    @Test
    public void oneFailedFetching() {
        final SyncComparator syncComparator = new SyncComparator(mockFactory, mockTenacityStory);
        final TenacityConfigurationFetcher differentFetcher = mock(TenacityConfigurationFetcher.class);
        final TenacityConfiguration differentConfiguration = new TenacityConfiguration();
        differentConfiguration.setExecutionIsolationThreadTimeoutInMillis(9);
        final Instance differentInstance = new Instance("http://deploy-001.sjc1.yammer.com:9090", "ignored", true);

        when(mockTenacityStory.retrieve(serviceId, dependencyId))
                .thenReturn(Optional.of(new ServiceModel(serviceId, dependencyId)));
        when(mockTenacityStory.retrieveLatest(dependencyId, serviceId))
                .thenReturn(Optional.of(new DependencyModel(dependencyId, new DateTime(testTimestamp), DependencyEntity.defaultConfiguration(), "fooUser", serviceId)));
        when(mockFactory.create(any(Instance.class), any(TenacityPropertyKey.class))).thenReturn(mockFetcher);
        when(mockFactory.create(eq(differentInstance), any(TenacityPropertyKey.class)))
                .thenReturn(differentFetcher);
        when(mockFetcher.queue()).thenReturn(Futures.immediateFuture(Optional.of(new TenacityConfiguration())));
        when(differentFetcher.queue()).thenReturn(Futures.immediateFuture(Optional.<TenacityConfiguration>absent()));

        assertThat(syncComparator.inSync(serviceId, dependencyId))
                .isEqualTo(allSynchronizedExceptUnknown(differentInstance));
    }

    @Test
    public void failedToFetchConfigurationsFromServices() {
        final SyncComparator syncComparator = new SyncComparator(mockFactory, mockTenacityStory);

        when(mockTenacityStory.retrieve(serviceId, dependencyId))
                .thenReturn(Optional.of(new ServiceModel(serviceId, dependencyId)));
        when(mockTenacityStory.retrieveLatest(dependencyId, serviceId))
                .thenReturn(Optional.of(new DependencyModel(dependencyId, new DateTime(testTimestamp), DependencyEntity.defaultConfiguration(), "fooUser", serviceId)));
        when(mockFactory.create(any(Instance.class), any(TenacityPropertyKey.class))).thenReturn(mockFetcher);
        when(mockFetcher.queue()).thenReturn(Futures.immediateFuture(Optional.<TenacityConfiguration>absent()));

        assertThat(syncComparator.inSync(serviceId, dependencyId))
                .isEqualTo(unknown());
    }
}
