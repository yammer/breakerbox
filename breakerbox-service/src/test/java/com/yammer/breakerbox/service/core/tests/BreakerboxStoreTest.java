package com.yammer.breakerbox.service.core.tests;

import com.google.common.base.Optional;
import com.yammer.azure.core.TableType;
import com.yammer.breakerbox.service.azure.DependencyEntity;
import com.yammer.breakerbox.service.azure.DependencyEntityData;
import com.yammer.breakerbox.service.azure.ServiceEntity;
import com.yammer.breakerbox.service.core.BreakerboxStore;
import com.yammer.breakerbox.service.core.DependencyId;
import com.yammer.breakerbox.service.core.ServiceId;
import com.yammer.breakerbox.service.tests.AbstractTestWithConfiguration;
import com.yammer.tenacity.core.config.CircuitBreakerConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.config.ThreadPoolConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

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
        timestamp = 1337000000l;
        dependencyConfiguration = new TenacityConfiguration(new ThreadPoolConfiguration(12, 23, 34, 45, 56, 67), new CircuitBreakerConfiguration(1, 2, 3, 4, 5), 6789);
        user = "USER";
    }

    @After
    public void teardown() {
        removeEntryIfPresent(breakerboxStore.retrieve(testServiceId, testDependencyId));
        removeEntryIfPresent(breakerboxStore.retrieve(testDependencyId, timestamp));
    }

    private void removeEntryIfPresent(Optional<? extends TableType> service) {
        if (service.isPresent()) {
            assertTrue(breakerboxStore.remove(service.get()));
        }
    }

    @Test
    public void testListServices() {
        assertTrue(breakerboxStore.storeServiceEntity(testServiceId, testDependencyId));
        assertThat(breakerboxStore.listServices())
                .contains(ServiceEntity.build(testServiceId, testDependencyId));
    }

    @Test
    public void testListDependencies() {
        assertTrue(breakerboxStore.storeServiceEntity(testServiceId, testDependencyId));
        assertThat(breakerboxStore.listDependencies(testServiceId))
                .contains(ServiceEntity.build(testServiceId, testDependencyId));
        assertThat(breakerboxStore.listDependencies(ServiceId.from(UUID.randomUUID().toString())))
                .isEmpty();
    }

    @Test
    public void testGetDependencyConfigurations(){
        assertTrue(breakerboxStore.storeDependencyEntity(testDependencyId, timestamp, dependencyConfiguration, user));
        assertThat(breakerboxStore.listDependencyConfigurations(testDependencyId))
                .contains(DependencyEntity.build(testDependencyId, DependencyEntityData.create(timestamp, user, dependencyConfiguration)));
        assertThat(breakerboxStore.listDependencyConfigurations(DependencyId.from(UUID.randomUUID().toString())))
                .isEmpty();
    }


}
