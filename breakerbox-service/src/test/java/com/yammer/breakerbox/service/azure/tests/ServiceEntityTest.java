package com.yammer.breakerbox.service.azure.tests;

import com.google.common.base.Optional;
import com.yammer.breakerbox.service.azure.ServiceEntity;
import com.yammer.breakerbox.service.core.tests.TableClientTestUtils;
import com.yammer.breakerbox.service.tests.AbstractTestWithConfiguration;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class ServiceEntityTest extends AbstractTestWithConfiguration {
    private ServiceId testServiceId;
    private DependencyId testDependencyId;

    @Before
    public void setup() {
        testServiceId = ServiceId.from(UUID.randomUUID().toString());
        testDependencyId = DependencyId.from(UUID.randomUUID().toString());
    }

    @After
    public void tearDown() {
        TableClientTestUtils.tearDownTestTable(tableClient, ServiceEntity.build(testServiceId, testDependencyId));
    }

    @Test
    public void canInsert() {
        final ServiceEntity entity = ServiceEntity.build(testServiceId, testDependencyId);
        assertTrue(tableClient.insert(entity));

        final Optional<ServiceEntity> retrieveEntity = tableClient.retrieve(entity);
        assertThat(retrieveEntity).isEqualTo(Optional.of(entity));

        assertThat(retrieveEntity.get().getServiceId()).isEqualTo(testServiceId);
        assertThat(retrieveEntity.get().getDependencyId()).isEqualTo(testDependencyId);
    }
}
