package com.yammer.breakerbox.jdbi.tests;

import com.google.common.base.Optional;
import com.yammer.breakerbox.jdbi.JdbiStore;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.store.model.DependencyModel;
import com.yammer.breakerbox.store.model.ServiceModel;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class JdbiStoreTest extends H2Test {
    private JdbiStore jdbiStore;

    @Before
    public void setup() throws Exception {
        jdbiStore = new JdbiStore(hsqlConfig, mock(Environment.class), database);
        jdbiStore.initialize();
    }

    @Test
    public void simpleStoreAndRetrieveService() {
        final ServiceModel serviceModel = serviceModel();
        assertTrue(jdbiStore.store(serviceModel));
        assertThat(jdbiStore.retrieve(serviceModel.getServiceId(), serviceModel.getDependencyId()))
                .isEqualTo(Optional.of(serviceModel));
    }

    @Test
    public void simpleStoreAndRetrieveDependency() {
        final DependencyModel dependencyModel = dependencyModel();
        assertTrue(jdbiStore.store(dependencyModel));
        assertThat(jdbiStore.retrieve(dependencyModel.getDependencyId(), dependencyModel.getDateTime()))
                .isEqualTo(Optional.of(dependencyModel));
    }

    @Test
    public void storeServiceTwiceShouldWork() {
        final ServiceModel serviceModel = serviceModel();
        assertTrue(jdbiStore.store(serviceModel));
        assertTrue(jdbiStore.store(serviceModel));
        assertThat(jdbiStore.retrieve(serviceModel.getServiceId(), serviceModel.getDependencyId()))
                .isEqualTo(Optional.of(serviceModel));
    }

    @Test
    public void storeDependencyTwiceShouldWork() {
        final DependencyModel dependencyModel = dependencyModel();
        assertTrue(jdbiStore.store(dependencyModel));
        assertTrue(jdbiStore.store(dependencyModel));
        assertThat(jdbiStore.retrieve(dependencyModel.getDependencyId(), dependencyModel.getDateTime()))
                .isEqualTo(Optional.of(dependencyModel));
    }

    @Test
    public void simpleDeleteService() {
        final ServiceModel serviceModel = serviceModel();
        assertTrue(jdbiStore.store(serviceModel));
        assertThat(jdbiStore.retrieve(serviceModel.getServiceId(), serviceModel.getDependencyId()))
                .isEqualTo(Optional.of(serviceModel));
        assertTrue(jdbiStore.delete(serviceModel));
        assertThat(jdbiStore.retrieve(serviceModel.getServiceId(), serviceModel.getDependencyId()))
                .isEqualTo(Optional.<ServiceModel>absent());
    }

    @Test
    public void simpleDeleteDependency() {
        final DependencyModel dependencyModel = dependencyModel();
        assertThat(jdbiStore.retrieve(dependencyModel.getDependencyId(), dependencyModel.getDateTime()))
                .isEqualTo(Optional.<DependencyModel>absent());

        assertTrue(jdbiStore.store(dependencyModel));
        assertThat(jdbiStore.retrieve(dependencyModel.getDependencyId(), dependencyModel.getDateTime()))
                .isEqualTo(Optional.of(dependencyModel));
        assertTrue(jdbiStore.delete(dependencyModel));
        assertThat(jdbiStore.retrieve(dependencyModel.getDependencyId(), dependencyModel.getDateTime()))
                .isEqualTo(Optional.<DependencyModel>absent());

        assertTrue(jdbiStore.store(dependencyModel));
        assertThat(jdbiStore.retrieve(dependencyModel.getDependencyId(), dependencyModel.getDateTime()))
                .isEqualTo(Optional.of(dependencyModel));
        assertTrue(jdbiStore.delete(dependencyModel.getDependencyId(), dependencyModel.getDateTime()));
        assertThat(jdbiStore.retrieve(dependencyModel.getDependencyId(), dependencyModel.getDateTime()))
                .isEqualTo(Optional.<DependencyModel>absent());

    }

    @Test
    public void deleteServiceTwiceShouldWork() {
        final ServiceModel serviceModel = serviceModel();
        assertTrue(jdbiStore.store(serviceModel));
        assertTrue(jdbiStore.delete(serviceModel));
        assertTrue(jdbiStore.delete(serviceModel));
    }

    @Test
    public void deleteDependencyTwiceShouldWork() {
        final DependencyModel dependencyModel = dependencyModel();
        assertThat(jdbiStore.retrieve(dependencyModel.getDependencyId(), dependencyModel.getDateTime()))
                .isEqualTo(Optional.<DependencyModel>absent());

        assertTrue(jdbiStore.store(dependencyModel));
        assertTrue(jdbiStore.delete(dependencyModel));
        assertTrue(jdbiStore.delete(dependencyModel));
    }

    @Test
    public void all() {
        assertThat(jdbiStore.allServiceModels()).isEmpty();
        final ServiceModel serviceModel1 = serviceModel();
        assertTrue(jdbiStore.store(serviceModel1));
        assertThat(jdbiStore.allServiceModels()).containsOnly(serviceModel1);
        final ServiceModel serviceModel2 = serviceModel();
        assertTrue(jdbiStore.store(serviceModel2));
        assertThat(jdbiStore.allServiceModels()).containsOnly(serviceModel1, serviceModel2);
    }

    @Test
    public void allMatchingAnId() {
        final ServiceId serviceId = ServiceId.from(UUID.randomUUID().toString());
        assertThat(jdbiStore.listDependenciesFor(serviceId)).isEmpty();
        final ServiceModel serviceModel1 = new ServiceModel(serviceId, DependencyId.from(UUID.randomUUID().toString()));
        assertTrue(jdbiStore.store(serviceModel1));
        assertThat(jdbiStore.listDependenciesFor(serviceId)).containsOnly(serviceModel1);
        final ServiceModel serviceModel2 = new ServiceModel(serviceId, DependencyId.from(UUID.randomUUID().toString()));
        assertTrue(jdbiStore.store(serviceModel2));
        assertThat(jdbiStore.listDependenciesFor(serviceId)).containsOnly(serviceModel1, serviceModel2);
        assertTrue(jdbiStore.store(serviceModel()));
        assertThat(jdbiStore.listDependenciesFor(serviceId)).containsOnly(serviceModel1, serviceModel2);
    }

    @Test
    public void retrieveLatest() {
        final DateTime now = DateTime.now();
        final DependencyModel earlyDependencyModel = dependencyModel(now);
        final DependencyModel laterDependencyModel = new DependencyModel(
                earlyDependencyModel.getDependencyId(),
                now.plusMinutes(1),
                earlyDependencyModel.getTenacityConfiguration(),
                earlyDependencyModel.getUser(),
                earlyDependencyModel.getServiceId());
        final DependencyModel superEarlyModel = new DependencyModel(
                earlyDependencyModel.getDependencyId(),
                now.minusMinutes(1),
                earlyDependencyModel.getTenacityConfiguration(),
                earlyDependencyModel.getUser(),
                earlyDependencyModel.getServiceId());
        assertThat(jdbiStore.retrieveLatest(earlyDependencyModel.getDependencyId(), earlyDependencyModel.getServiceId()))
                .isEqualTo(Optional.<DependencyModel>absent());
        assertTrue(jdbiStore.store(earlyDependencyModel));
        assertThat(jdbiStore.retrieveLatest(earlyDependencyModel.getDependencyId(), earlyDependencyModel.getServiceId()))
                .isEqualTo(Optional.of(earlyDependencyModel));
        assertTrue(jdbiStore.store(laterDependencyModel));
        assertThat(jdbiStore.retrieveLatest(earlyDependencyModel.getDependencyId(), earlyDependencyModel.getServiceId()))
                .isEqualTo(Optional.of(laterDependencyModel));
        assertTrue(jdbiStore.store(superEarlyModel));
        assertThat(jdbiStore.retrieveLatest(earlyDependencyModel.getDependencyId(), earlyDependencyModel.getServiceId()))
                .isEqualTo(Optional.of(laterDependencyModel));
    }

    @Test
    public void allDependenciesForService() {
        final DependencyModel dependencyModel1 = dependencyModel();
        assertTrue(jdbiStore.store(dependencyModel1));
        assertThat(jdbiStore.allDependenciesFor(dependencyModel1.getDependencyId(), dependencyModel1.getServiceId()))
                .containsOnly(dependencyModel1);
        final DependencyModel dependencyModel2 = new DependencyModel(
                dependencyModel1.getDependencyId(),
                dependencyModel1.getDateTime().plusMinutes(1),
                dependencyModel1.getTenacityConfiguration(),
                UUID.randomUUID().toString(),
                dependencyModel1.getServiceId());
        assertTrue(jdbiStore.store(dependencyModel2));
        assertThat(jdbiStore.allDependenciesFor(dependencyModel1.getDependencyId(), dependencyModel1.getServiceId()))
                .containsOnly(dependencyModel1, dependencyModel2);
        assertTrue(jdbiStore.store(dependencyModel()));
        assertThat(jdbiStore.allDependenciesFor(dependencyModel1.getDependencyId(), dependencyModel1.getServiceId()))
                .containsOnly(dependencyModel1, dependencyModel2);
    }

    @Test
    public void storingSameObjectReturnsTrueOtherwiseFalse() {
        final DependencyModel dependencyModel = dependencyModel();
        assertTrue(jdbiStore.store(dependencyModel));
        assertTrue(jdbiStore.store(dependencyModel));
        assertFalse(jdbiStore.store(new DependencyModel(
                dependencyModel.getDependencyId(),
                dependencyModel.getDateTime(),
                new TenacityConfiguration(),
                UUID.randomUUID().toString(),
                ServiceId.from(UUID.randomUUID().toString()))));
    }
}