package com.yammer.breakerbox.jdbi.tests;

import com.google.common.base.Optional;
import com.yammer.breakerbox.jdbi.JdbiConfiguration;
import com.yammer.breakerbox.jdbi.JdbiStore;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.store.model.ServiceModel;
import com.yammer.dropwizard.config.Environment;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class JdbiStoreTest extends H2Test {
    private JdbiStore jdbiStore;

    @Before
    public void setup() {
        jdbiStore = new JdbiStore(new JdbiConfiguration(), mock(Environment.class), database);
    }

    private static ServiceModel serviceModel() {
        return new ServiceModel(
                ServiceId.from(UUID.randomUUID().toString()),
                DependencyId.from(UUID.randomUUID().toString()));
    }

    @Test
    public void simpleStoreAndRetrieve() {
        final ServiceModel serviceModel = serviceModel();
        assertTrue(jdbiStore.store(serviceModel));
        assertThat(jdbiStore.retrieve(serviceModel.getServiceId(), serviceModel.getDependencyId()))
                .isEqualTo(Optional.of(serviceModel));
    }
}
