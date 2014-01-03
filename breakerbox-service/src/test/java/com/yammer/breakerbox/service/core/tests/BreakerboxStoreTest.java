package com.yammer.breakerbox.service.core.tests;

import com.google.common.base.Optional;
import com.yammer.azure.core.TableType;
import com.yammer.breakerbox.service.azure.DependencyEntity;
import com.yammer.breakerbox.service.azure.ServiceEntity;
import com.yammer.breakerbox.service.core.BreakerboxStore;
import com.yammer.breakerbox.service.tests.AbstractTestWithConfiguration;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.tenacity.core.config.CircuitBreakerConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.config.ThreadPoolConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class BreakerboxStoreTest extends AbstractTestWithConfiguration {
    private BreakerboxStore breakerboxStore;
    private ServiceId testServiceId;
    private DependencyId testDependencyId;
    private TenacityConfiguration dependencyConfiguration;
    private long timestamp;
    private String user;

    @Before
    public void setup() {
        breakerboxStore = new BreakerboxStore(tableClient);
        testServiceId = ServiceId.from(UUID.randomUUID().toString());
        testDependencyId = DependencyId.from(UUID.randomUUID().toString());
        timestamp = 1345938944000l;
        dependencyConfiguration = new TenacityConfiguration(new ThreadPoolConfiguration(12, 23, 34, 45, 56, 67), new CircuitBreakerConfiguration(1, 2, 3, 4, 5), 6789);
        user = "USER";
    }

    @After
    public void teardown() {
        removeEntryIfPresent(breakerboxStore.retrieve(testServiceId, testDependencyId));
        removeEntryIfPresent(breakerboxStore.retrieve(testDependencyId, timestamp, testServiceId));
        removeEntryIfPresent(breakerboxStore.retrieveLatest(testDependencyId, testServiceId));
    }

    private void removeEntryIfPresent(Optional<? extends TableType> service) {
        if (service.isPresent()) {
            assertTrue(breakerboxStore.remove(service.get()));
        }
    }

    @Test
    public void testListServices() {
        assertTrue(breakerboxStore.store(testServiceId, testDependencyId));
        assertThat(breakerboxStore.listServices())
                .contains(ServiceEntity.build(testServiceId, testDependencyId));
    }

    @Test
    public void testListDependencies() {
        assertTrue(breakerboxStore.store(testServiceId, testDependencyId));
        assertThat(breakerboxStore.listDependencies(testServiceId))
                .contains(ServiceEntity.build(testServiceId, testDependencyId));
        assertThat(breakerboxStore.listDependencies(ServiceId.from(UUID.randomUUID().toString())))
                .isEmpty();
    }

    @Test
    public void testGetDependencyConfigurations(){
        final DependencyEntity dependencyEntity = DependencyEntity.build(testDependencyId, timestamp, user, dependencyConfiguration, testServiceId);
        assertTrue(tableClient.insertOrReplace(dependencyEntity));
        assertEquals(breakerboxStore.retrieve(testDependencyId, timestamp, testServiceId), Optional.of(dependencyEntity));
        assertEquals(breakerboxStore.retrieveLatest(testDependencyId, testServiceId), Optional.of(dependencyEntity));
        assertThat(breakerboxStore.listConfigurations(testDependencyId, testServiceId))
                .contains(DependencyEntity.build(testDependencyId, timestamp, user, dependencyConfiguration, testServiceId));
        assertThat(breakerboxStore.listConfigurations(
                DependencyId.from(UUID.randomUUID().toString()),
                ServiceId.from(UUID.randomUUID().toString())))
                .isEmpty();
    }

    @Test
    public void canStoreSameDependencyIdWithTwoDifferentServiceIds() {
        final DependencyId dependencyIdOne = DependencyId.from(UUID.randomUUID().toString());
        final DependencyId dependencyIdTwo = DependencyId.from(UUID.randomUUID().toString());

        try {
            final DependencyEntity dependencyEntityOne = DependencyEntity.build(dependencyIdOne, timestamp, user, dependencyConfiguration, testServiceId);
            final DependencyEntity dependencyEntityTwo = DependencyEntity.build(dependencyIdTwo, timestamp, user, DependencyEntity.defaultConfiguration(), testServiceId);
            assertTrue(tableClient.insertOrReplace(dependencyEntityOne));
            assertTrue(tableClient.insertOrReplace(dependencyEntityTwo));
            assertThat(breakerboxStore.listConfigurations(dependencyIdOne, testServiceId))
                    .contains(dependencyEntityOne)
                    .doesNotContain(dependencyEntityTwo);
            assertThat(breakerboxStore.listConfigurations(dependencyIdTwo, testServiceId))
                    .contains(dependencyEntityTwo)
                    .doesNotContain(dependencyEntityOne);
        } finally {
            removeEntryIfPresent(breakerboxStore.retrieveLatest(dependencyIdOne, testServiceId));
            removeEntryIfPresent(breakerboxStore.retrieveLatest(dependencyIdTwo, testServiceId));
        }
    }

    @Test
    public void storeUpdatesBothEntities() {
        assertTrue(breakerboxStore.store(testServiceId, testDependencyId, new TenacityConfiguration(), user));
        assertEquals(breakerboxStore.retrieve(testServiceId, testDependencyId), Optional.of(ServiceEntity.build(testServiceId, testDependencyId)));
        final Optional<DependencyEntity> dependencyEntity = breakerboxStore.retrieveLatest(testDependencyId, testServiceId);
        assertNotEquals(dependencyEntity, Optional.absent());
        assertEquals(dependencyEntity.get(),
                DependencyEntity.build(
                        testDependencyId,
                        dependencyEntity.get().getConfigurationTimestamp(),
                        user,
                        new TenacityConfiguration(),
                        testServiceId));
        assertEquals(breakerboxStore.retrieve(testDependencyId, dependencyEntity.get().getConfigurationTimestamp(), testServiceId),
                dependencyEntity);
    }


}
