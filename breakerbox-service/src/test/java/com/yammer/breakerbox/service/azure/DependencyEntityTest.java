package com.yammer.breakerbox.service.azure;

import com.google.common.base.Optional;
import com.microsoft.windowsazure.services.table.client.TableServiceEntity;
import com.yammer.breakerbox.service.core.DependencyId;
import com.yammer.breakerbox.service.core.tests.TableClientTestUtils;
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

public class DependencyEntityTest extends AbstractTestWithConfiguration {

    private DependencyId dependencyId;
    private long testTimeStamp;
    private String user;

    @Before
    public void setup() {
        dependencyId = DependencyId.from(UUID.randomUUID().toString());
        testTimeStamp = System.currentTimeMillis();
        user = "USER";
    }

    @After
    public void tearDown() {
        TableClientTestUtils.tearDownTestTable(tableClient, DependencyEntity.build(dependencyId, DependencyEntityData.createDefaultConfiguration(testTimeStamp, user)));
    }

    @Test
    public void testCanInsert() throws Exception {
        final DependencyEntity entity = DependencyEntity.build(dependencyId, DependencyEntityData.createDefaultConfiguration(testTimeStamp, user));
        final boolean success = tableClient.insert(entity);
        assertTrue(success);

        final Optional<TableServiceEntity> retrieve = tableClient.retrieve(DependencyEntity.build(dependencyId, DependencyEntityData.createDefaultConfiguration(testTimeStamp, user)));
        assertTrue(retrieve.isPresent());
        assertThat(retrieve.get()).isEqualTo(entity);
    }

    @Test
    public void testSerializationAndDeserializationOfConfig() throws Exception {
        final TenacityConfiguration dependencyConfiguration = new TenacityConfiguration(new ThreadPoolConfiguration(12, 23, 34, 45, 56, 67), new CircuitBreakerConfiguration(1, 2, 3, 4, 5), 6789);//numbers totally arbitrary
        final DependencyEntity entry = DependencyEntity.build(dependencyId, DependencyEntityData.create(testTimeStamp, user, dependencyConfiguration));

        final TenacityConfiguration recomposedConfiguration = entry.getDependencyTableEntry().get().getConfiguration();
        assertThat(recomposedConfiguration).isEqualsToByComparingFields(dependencyConfiguration);

    }
}
