package com.yammer.breakerbox.azure.model.tests;

import com.google.common.base.Optional;
import com.yammer.breakerbox.azure.TableClient;
import com.yammer.breakerbox.azure.TableClientFactory;
import com.yammer.breakerbox.azure.model.ServiceEntity;
import com.yammer.breakerbox.azure.tests.TableClientTester;
import com.yammer.breakerbox.azure.tests.WithConfiguration;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class ServiceEntityTest extends WithConfiguration {
    private ServiceId testServiceId;
    private DependencyId testDependencyId;
    private TableClient tableClient;
    private TableClientTester tableClientTester;

    @Before
    public void setup() {
        testServiceId = ServiceId.from(UUID.randomUUID().toString());
        testDependencyId = DependencyId.from(UUID.randomUUID().toString());
        tableClient = new TableClientFactory(azureTableConfiguration).create();
        tableClientTester = new TableClientTester(tableClient);
    }

    @After
    public void tearDown() {
        tableClientTester.remove(ServiceEntity.build(testServiceId, testDependencyId));
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
