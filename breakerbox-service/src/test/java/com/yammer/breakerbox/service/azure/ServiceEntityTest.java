package com.yammer.breakerbox.service.azure;

import com.google.common.base.Optional;
import com.yammer.breakerbox.service.core.ServiceId;
import com.yammer.breakerbox.service.tests.AbstractTestWithConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class ServiceEntityTest extends AbstractTestWithConfiguration {
    private ServiceId testServiceId;

    @Before
    public void setup() {
        testServiceId = ServiceId.from(UUID.randomUUID().toString());
    }

    @After
    public void teardown() {
        final Optional<ServiceEntity> entity = tableClient.retrieve(new ServiceEntity(testServiceId));
        if (entity.isPresent()) {
            assertTrue(tableClient.remove(entity.get()));
        }
    }

    @Test
    public void canInsert() {
        final ServiceEntity entity = new ServiceEntity(testServiceId);
        assertTrue(tableClient.insert(entity));

        final Optional<ServiceEntity> retrieveEntity = tableClient.retrieve(entity);
        assertThat(retrieveEntity).isEqualTo(Optional.of(entity));
    }
}