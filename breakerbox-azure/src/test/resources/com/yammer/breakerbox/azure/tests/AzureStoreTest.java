package com.yammer.breakerbox.azure.tests;

import com.google.common.base.Optional;
import com.yammer.breakerbox.azure.AzureStore;
import com.yammer.breakerbox.azure.TableClient;
import com.yammer.breakerbox.azure.TableClientFactory;
import com.yammer.breakerbox.azure.model.DependencyEntity;
import com.yammer.breakerbox.azure.model.Entities;
import com.yammer.breakerbox.store.BreakerboxStore;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.store.model.DependencyModel;
import com.yammer.breakerbox.store.model.ServiceModel;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.core.config.CircuitBreakerConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.config.ThreadPoolConfiguration;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class AzureStoreTest extends WithConfiguration {
    private BreakerboxStore breakerboxStore;
    private TableClient tableClient;
    private ServiceId testServiceId;
    private DependencyId testDependencyId;
    private TenacityConfiguration dependencyConfiguration;
    private long timestamp;
    private String user;

    @Before
    public void setup() {
        tableClient = new TableClientFactory(azureTableConfiguration).create();
        breakerboxStore = new AzureStore(azureTableConfiguration, mock(Environment.class));
        assertTrue(breakerboxStore.initialize());
        testServiceId = ServiceId.from(UUID.randomUUID().toString());
        testDependencyId = DependencyId.from(UUID.randomUUID().toString());
        timestamp = 1345938944000l;
        dependencyConfiguration = new TenacityConfiguration(new ThreadPoolConfiguration(12, 23, 34, 45, 56, 67), new CircuitBreakerConfiguration(1, 2, 3, 4, 5), 6789);
        user = "USER";
    }

    @After
    public void teardown() {
        assertTrue(breakerboxStore.delete(new ServiceModel(testServiceId, testDependencyId)));
        assertTrue(breakerboxStore.delete(testServiceId, testDependencyId));
        assertTrue(breakerboxStore.delete(testDependencyId, new DateTime(timestamp), testServiceId));
        removeDependencyModel(breakerboxStore.retrieveLatest(testDependencyId, testServiceId));
    }

    private void removeDependencyModel(Optional<DependencyModel> dependencyModel) {
        if (dependencyModel.isPresent()) {
            assertTrue(breakerboxStore.delete(dependencyModel.get()));
        }
    }

    @Test
    public void testListServices() {
        assertTrue(breakerboxStore.store(new ServiceModel(testServiceId, testDependencyId)));
        assertThat(breakerboxStore.allServiceModels())
                .contains(new ServiceModel(testServiceId, testDependencyId));
    }

    @Test
    public void testListDependencies() {
        assertTrue(breakerboxStore.store(new ServiceModel(testServiceId, testDependencyId)));
        assertThat(breakerboxStore.listDependenciesFor(testServiceId))
                .contains(new ServiceModel(testServiceId, testDependencyId));
        assertThat(breakerboxStore.listDependenciesFor(ServiceId.from(UUID.randomUUID().toString())))
                .isEmpty();
    }

    @Test
    public void testGetDependencyConfigurations(){
        final DependencyEntity dependencyEntity = DependencyEntity.build(testDependencyId, timestamp, user, dependencyConfiguration, testServiceId);
        final Optional<DependencyModel> expectedModel = Optional.of(Entities.toModel(dependencyEntity));
        assertTrue(tableClient.insertOrReplace(dependencyEntity));
        assertEquals(breakerboxStore.retrieve(testDependencyId, new DateTime(timestamp), testServiceId), expectedModel);
        assertEquals(breakerboxStore.retrieveLatest(testDependencyId, testServiceId), expectedModel);
        assertThat(breakerboxStore.allDependenciesFor(testDependencyId, testServiceId))
                .contains(expectedModel.get());
        assertThat(breakerboxStore.allDependenciesFor(
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
            assertThat(breakerboxStore.allDependenciesFor(dependencyIdOne, testServiceId))
                    .contains(Entities.toModel(dependencyEntityOne))
                    .doesNotContain(Entities.toModel(dependencyEntityTwo));
            assertThat(breakerboxStore.allDependenciesFor(dependencyIdTwo, testServiceId))
                    .contains(Entities.toModel(dependencyEntityTwo))
                    .doesNotContain(Entities.toModel(dependencyEntityOne));
        } finally {
            removeDependencyModel(breakerboxStore.retrieveLatest(dependencyIdOne, testServiceId));
            removeDependencyModel(breakerboxStore.retrieveLatest(dependencyIdTwo, testServiceId));
        }
    }

    @Test
    public void updateBothEntitiesTogether() {
        assertTrue(breakerboxStore.store(new ServiceModel(testServiceId, testDependencyId)));
        assertTrue(breakerboxStore.store(new DependencyModel(testDependencyId, DateTime.now(), new TenacityConfiguration(), user, testServiceId)));
        assertEquals(breakerboxStore.retrieve(testServiceId, testDependencyId), Optional.of(new ServiceModel(testServiceId, testDependencyId)));
        final Optional<DependencyModel> dependencyModel = breakerboxStore.retrieveLatest(testDependencyId, testServiceId);
        assertNotEquals(dependencyModel, Optional.absent());
        assertEquals(dependencyModel.get(), new DependencyModel(
                testDependencyId,
                dependencyModel.get().getDateTime(),
                new TenacityConfiguration(),
                user,
                testServiceId));
        assertEquals(breakerboxStore.retrieve(testDependencyId, dependencyModel.get().getDateTime(), testServiceId),
                dependencyModel);
    }
}