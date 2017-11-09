package com.yammer.breakerbox.azure.model.tests;

import com.microsoft.azure.storage.table.TableServiceEntity;
import com.yammer.breakerbox.azure.TableClient;
import com.yammer.breakerbox.azure.TableClientFactory;
import com.yammer.breakerbox.azure.model.DependencyEntity;
import com.yammer.breakerbox.azure.tests.TableClientTester;
import com.yammer.breakerbox.azure.tests.WithConfiguration;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.tenacity.core.config.CircuitBreakerConfiguration;
import com.yammer.tenacity.core.config.SemaphoreConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.config.ThreadPoolConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class DependencyEntityTest extends WithConfiguration {
    private DependencyId dependencyId;
    private ServiceId serviceId;
    private long testTimeStamp;
    private String user;
    private TableClient tableClient;
    private TableClientTester tableClientTester;

    @Before
    public void setup() {
        dependencyId = DependencyId.from(UUID.randomUUID().toString());
        serviceId = ServiceId.from(UUID.randomUUID().toString());
        testTimeStamp = System.currentTimeMillis();
        user = "USER";
        tableClient = new TableClientFactory(azureTableConfiguration).create();
        tableClientTester = new TableClientTester(tableClient);
        assumeTrue(validAzureAccount());
    }

    @After
    public void tearDown() {
        tableClientTester.remove(DependencyEntity.build(dependencyId, testTimeStamp, user, DependencyEntity.defaultConfiguration(), serviceId));
    }

    @Test
    public void testCanInsert() throws Exception {
        final DependencyEntity entity = DependencyEntity.build(dependencyId, testTimeStamp, user, new TenacityConfiguration(), serviceId);
        final boolean success = tableClient.insert(entity);
        assertTrue(success);

        final Optional<TableServiceEntity> retrieve = tableClient.retrieve(DependencyEntity.build(dependencyId, testTimeStamp, user, new TenacityConfiguration(), serviceId));
        assertTrue(retrieve.isPresent());
        assertThat(retrieve.get()).isEqualTo(entity);
    }

    @Test
    public void testSerializationAndDeserializationOfConfig() throws Exception {
        final TenacityConfiguration dependencyConfiguration = new TenacityConfiguration(new ThreadPoolConfiguration(12, 23, 34, 45, 56, 67), new CircuitBreakerConfiguration(1, 2, 3, 4, 5), new SemaphoreConfiguration(3, 4), 6789);//numbers totally arbitrary
        final DependencyEntity entry = DependencyEntity.build(dependencyId, testTimeStamp, user, dependencyConfiguration, serviceId);

        final TenacityConfiguration recomposedConfiguration = entry.getConfiguration().get();
        assertThat(recomposedConfiguration).isEqualToComparingFieldByField(dependencyConfiguration);

    }

    /**
     * Replacing isn't actually used at time of test writing, but it's a good one to keep in the client API, so testing it here.
     */
    @Test
    public void testCanReplace() throws Exception {
        final DependencyEntity originalEntity = DependencyEntity.build(dependencyId, testTimeStamp, user,
                new TenacityConfiguration(
                        new ThreadPoolConfiguration(),
                        new CircuitBreakerConfiguration(1234, 5678, 910, 20000, 10),
                        new SemaphoreConfiguration(),
                        3000),
                serviceId);

        assertTrue(tableClient.insertOrReplace(originalEntity));

        final TenacityConfiguration updatedConfiguration = new TenacityConfiguration(
                new ThreadPoolConfiguration(),
                new CircuitBreakerConfiguration(987, 6543, 321, 1000, 20),
                new SemaphoreConfiguration(),
                4000);
        final DependencyEntity updatedEntity = DependencyEntity.build(dependencyId, testTimeStamp, user,
                updatedConfiguration, serviceId);

        assertTrue(tableClient.insertOrReplace(updatedEntity));
        final Optional<DependencyEntity> retrievedEntity = tableClient.retrieve(originalEntity);
        assertTrue(retrievedEntity.isPresent());
        assertThat(retrievedEntity.get().getConfiguration()).isEqualTo(Optional.of(updatedConfiguration));
    }
}