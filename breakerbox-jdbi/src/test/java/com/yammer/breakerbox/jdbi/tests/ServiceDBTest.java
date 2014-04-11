package com.yammer.breakerbox.jdbi.tests;

import com.yammer.breakerbox.jdbi.ServiceDB;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.store.model.ServiceModel;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;

public class ServiceDBTest extends H2Test {
    private ServiceDB serviceDB;

    @Before
    public void setup() {
        serviceDB = database.onDemand(ServiceDB.class);
    }

    private static ServiceModel serviceModel() {
        return new ServiceModel(
                ServiceId.from(UUID.randomUUID().toString()),
                DependencyId.from(UUID.randomUUID().toString()));
    }

    @Test
    public void simpleStoreAndRetrieve() {
        final ServiceModel serviceModel = serviceModel();
        serviceDB.insert(serviceModel);
        assertThat(serviceDB.find(serviceModel)).isEqualTo(serviceModel);
    }
}