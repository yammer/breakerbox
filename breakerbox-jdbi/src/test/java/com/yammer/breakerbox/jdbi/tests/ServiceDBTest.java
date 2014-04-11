package com.yammer.breakerbox.jdbi.tests;

import com.yammer.breakerbox.jdbi.ServiceDB;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.store.model.ServiceModel;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.exceptions.DBIException;

import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;

public class ServiceDBTest extends H2Test {
    private ServiceDB serviceDB;

    @Before
    public void setup() {
        serviceDB = database.onDemand(ServiceDB.class);
    }

    @Test
    public void simpleStoreAndRetrieve() {
        final ServiceModel serviceModel = serviceModel();
        assertThat(serviceDB.insert(serviceModel)).isEqualTo(1);
        assertThat(serviceDB.find(serviceModel)).isEqualTo(serviceModel);
    }

    @Test(expected = DBIException.class)
    public void storeTwice() {
        final ServiceModel serviceModel = serviceModel();
        assertThat(serviceDB.insert(serviceModel)).isEqualTo(1);
        assertThat(serviceDB.insert(serviceModel)).isEqualTo(0);
    }

    @Test
    public void simpleDelete() {
        final ServiceModel serviceModel = serviceModel();
        assertThat(serviceDB.insert(serviceModel)).isEqualTo(1);
        assertThat(serviceDB.find(serviceModel)).isEqualTo(serviceModel);
        assertThat(serviceDB.delete(serviceModel)).isEqualTo(1);
        assertThat(serviceDB.find(serviceModel)).isNull();
    }

    @Test
    public void deleteTwice() {
        final ServiceModel serviceModel = serviceModel();
        assertThat(serviceDB.insert(serviceModel)).isEqualTo(1);
        assertThat(serviceDB.delete(serviceModel)).isEqualTo(1);
        assertThat(serviceDB.delete(serviceModel)).isEqualTo(0);
    }

    @Test
    public void all() {
        assertThat(serviceDB.all()).isEmpty();
        final ServiceModel serviceModel1 = serviceModel();
        assertThat(serviceDB.insert(serviceModel1)).isEqualTo(1);
        assertThat(serviceDB.all()).containsOnly(serviceModel1);
        final ServiceModel serviceModel2 = serviceModel();
        assertThat(serviceDB.insert(serviceModel2)).isEqualTo(1);
        assertThat(serviceDB.all()).containsOnly(serviceModel1, serviceModel2);
    }

    @Test
    public void allMatchingAnId() {
        final ServiceId serviceId = ServiceId.from(UUID.randomUUID().toString());
        assertThat(serviceDB.all(serviceId)).isEmpty();
        final ServiceModel serviceModel1 = new ServiceModel(serviceId, DependencyId.from(UUID.randomUUID().toString()));
        assertThat(serviceDB.insert(serviceModel1)).isEqualTo(1);
        assertThat(serviceDB.all(serviceId)).containsOnly(serviceModel1);
        final ServiceModel serviceModel2 = new ServiceModel(serviceId, DependencyId.from(UUID.randomUUID().toString()));
        assertThat(serviceDB.insert(serviceModel2)).isEqualTo(1);
        assertThat(serviceDB.all(serviceId)).containsOnly(serviceModel1, serviceModel2);
        assertThat(serviceDB.insert(serviceModel())).isEqualTo(1);
        assertThat(serviceDB.all(serviceId)).containsOnly(serviceModel1, serviceModel2);
    }
}