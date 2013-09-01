package com.yammer.breakerbox.service.core.tests;

import com.google.common.base.Optional;
import com.yammer.breakerbox.service.azure.ServiceEntity;
import com.yammer.breakerbox.service.core.BreakerboxStore;
import com.yammer.breakerbox.service.core.DependencyId;
import com.yammer.breakerbox.service.core.ServiceId;
import com.yammer.breakerbox.service.tests.AbstractTestWithConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class TenacityStoreTest extends AbstractTestWithConfiguration {
    private BreakerboxStore breakerboxStore;
    private ServiceId testServiceId;
    private DependencyId testDependencyId;

    @Before
    public void setup() {
        breakerboxStore = new BreakerboxStore(tableClient);
        testServiceId = ServiceId.from(UUID.randomUUID().toString());
        testDependencyId = DependencyId.from(UUID.randomUUID().toString());
    }

    @After
    public void teardown() {
        final Optional<ServiceEntity> entity = breakerboxStore.retrieve(testServiceId, testDependencyId);
        if (entity.isPresent()) {
            assertTrue(breakerboxStore.remove(entity.get()));
        }
    }

    @Test
    public void listServices() {
        assertTrue(breakerboxStore.store(testServiceId, testDependencyId));
        assertThat(breakerboxStore.listServices())
                .contains(ServiceEntity.build(testServiceId, testDependencyId));
    }

    @Test
    public void listDependencies() {
        assertTrue(breakerboxStore.store(testServiceId, testDependencyId));
        assertThat(breakerboxStore.listDependencies(testServiceId))
                .contains(ServiceEntity.build(testServiceId, testDependencyId));
        assertThat(breakerboxStore.listDependencies(ServiceId.from(UUID.randomUUID().toString())))
                .isEmpty();
    }
}
