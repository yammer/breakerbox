package com.yammer.breakerbox.service.core.tests;

import com.google.common.base.Optional;
import com.yammer.breakerbox.service.azure.ServiceEntity;
import com.yammer.breakerbox.service.core.DependencyId;
import com.yammer.breakerbox.service.core.ServiceId;
import com.yammer.breakerbox.service.core.TenacityStore;
import com.yammer.breakerbox.service.tests.AbstractTestWithConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class TenacityStoreTest extends AbstractTestWithConfiguration {
    private TenacityStore tenacityStore;
    private ServiceId testServiceId;
    private DependencyId testDependencyId;

    @Before
    public void setup() {
        tenacityStore = new TenacityStore(tableClient);
        testServiceId = ServiceId.from(UUID.randomUUID().toString());
        testDependencyId = DependencyId.from(UUID.randomUUID().toString());
    }

    @After
    public void teardown() {
        final Optional<ServiceEntity> entity = tenacityStore.retrieve(testServiceId, testDependencyId);
        if (entity.isPresent()) {
            assertTrue(tenacityStore.remove(entity.get()));
        }
    }

    @Test
    public void listServices() {
        assertTrue(tenacityStore.store(testServiceId, testDependencyId));
        assertThat(tenacityStore.listServices())
                .contains(ServiceEntity.build(testServiceId, testDependencyId));
    }

    @Test
    public void listDependencies() {
        assertTrue(tenacityStore.store(testServiceId, testDependencyId));
        assertThat(tenacityStore.listDependencies(testServiceId))
                .contains(ServiceEntity.build(testServiceId, testDependencyId));
        assertThat(tenacityStore.listDependencies(ServiceId.from(UUID.randomUUID().toString())))
                .isEmpty();
    }
}
