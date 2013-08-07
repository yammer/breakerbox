package com.yammer.breakerbox.service.azure;

import com.google.common.base.Optional;
import com.yammer.breakerbox.service.core.DependencyId;
import com.yammer.breakerbox.service.core.EnvironmentId;
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
    private EnvironmentId testEnvironmentId;
    private DependencyId testDependencyId;

    @Before
    public void setup() {
        testEnvironmentId = EnvironmentId.from(UUID.randomUUID().toString());
        testDependencyId = DependencyId.from(UUID.randomUUID().toString());
    }

    @After
    public void teardown() {
        final Optional<DependencyEntity> entity = tableClient.retrieve(DependencyEntity.key(testEnvironmentId, testDependencyId));
        if (entity.isPresent()) {
            assertTrue(tableClient.remove(entity.get()));
        }
    }

    @Test
    public void canInsert() {
        final DependencyEntity entity = DependencyEntity.build(new TenacityConfiguration(), testEnvironmentId, testDependencyId);
        assertThat(Optional.of(new TenacityConfiguration())).isEqualTo(entity.getTenacityConfiguration());
        assertTrue(tableClient.insertOrReplace(entity));
    }

    @Test
    public void canReplace() {
        final DependencyEntity entity = DependencyEntity.build(new TenacityConfiguration(), testEnvironmentId, testDependencyId);

        assertTrue(tableClient.insertOrReplace(entity));

        final TenacityConfiguration alteredConfiguration = new TenacityConfiguration(
                new ThreadPoolConfiguration(),
                new CircuitBreakerConfiguration(1234, 5678, 910, 20000, 10),
                3000);

        assertTrue(tableClient.insertOrReplace(entity.using(alteredConfiguration)));
        final Optional<DependencyEntity> retrievedEntity = tableClient.retrieve(entity.key());
        assertTrue(retrievedEntity.isPresent());
        assertThat(retrievedEntity.get().getTenacityConfiguration()).isEqualTo(Optional.of(alteredConfiguration));
    }
}
