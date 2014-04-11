package com.yammer.breakerbox.jdbi.tests;

import com.yammer.breakerbox.jdbi.DependencyDB;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.model.DependencyModel;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.exceptions.DBIException;

import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;

public class DependencyDBTest extends H2Test {
    private DependencyDB dependencyDB;

    @Before
    public void setup() {
        dependencyDB = database.onDemand(DependencyDB.class);
    }

    @Test
    public void storeAndRetrive() {
        assertThat(dependencyDB.find(DependencyId.from(UUID.randomUUID().toString()), DateTime.now())).isNull();
        final DependencyModel dependencyModel = dependencyModel();
        assertThat(dependencyDB.insert(dependencyModel)).isEqualTo(1);
        assertThat(dependencyDB.find(dependencyModel.getDependencyId(), dependencyModel.getDateTime()))
                .isEqualTo(dependencyModel);
    }

    @Test(expected = DBIException.class)
    public void storeTwice() {
        final DependencyModel dependencyModel = dependencyModel();
        assertThat(dependencyDB.insert(dependencyModel)).isEqualTo(1);
        assertThat(dependencyDB.insert(dependencyModel)).isEqualTo(0);
    }

    @Test
    public void simpleDelete() {
        final DependencyModel dependencyModel = dependencyModel();
        assertThat(dependencyDB.insert(dependencyModel)).isEqualTo(1);
        assertThat(dependencyDB.find(dependencyModel.getDependencyId(), dependencyModel.getDateTime())).isEqualTo(dependencyModel);
        assertThat(dependencyDB.delete(dependencyModel.getDependencyId(), dependencyModel.getDateTime())).isEqualTo(1);
        assertThat(dependencyDB.find(dependencyModel.getDependencyId(), dependencyModel.getDateTime())).isNull();
    }

    @Test
    public void deleteTwice() {
        final DependencyModel dependencyModel = dependencyModel();
        assertThat(dependencyDB.insert(dependencyModel)).isEqualTo(1);
        assertThat(dependencyDB.delete(dependencyModel.getDependencyId(), dependencyModel.getDateTime())).isEqualTo(1);
        assertThat(dependencyDB.delete(dependencyModel.getDependencyId(), dependencyModel.getDateTime())).isEqualTo(0);
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
        assertThat(dependencyDB.findLatest(earlyDependencyModel.getDependencyId(), earlyDependencyModel.getServiceId()))
                .isNull();
        assertThat(dependencyDB.insert(earlyDependencyModel)).isEqualTo(1);
        assertThat(dependencyDB.findLatest(earlyDependencyModel.getDependencyId(), earlyDependencyModel.getServiceId()))
                .isEqualTo(earlyDependencyModel);
        assertThat(dependencyDB.insert(laterDependencyModel)).isEqualTo(1);
        assertThat(dependencyDB.findLatest(earlyDependencyModel.getDependencyId(), earlyDependencyModel.getServiceId()))
                .isEqualTo(laterDependencyModel);
        assertThat(dependencyDB.insert(superEarlyModel)).isEqualTo(1);
        assertThat(dependencyDB.findLatest(earlyDependencyModel.getDependencyId(), earlyDependencyModel.getServiceId()))
                .isEqualTo(laterDependencyModel);
    }

    @Test
    public void allDependenciesForService() {
        final DependencyModel dependencyModel1 = dependencyModel();
        assertThat(dependencyDB.insert(dependencyModel1)).isEqualTo(1);
        assertThat(dependencyDB.all(dependencyModel1.getDependencyId(), dependencyModel1.getServiceId()))
                .containsOnly(dependencyModel1);
        final DependencyModel dependencyModel2 = new DependencyModel(
                dependencyModel1.getDependencyId(),
                dependencyModel1.getDateTime().plusMinutes(1),
                dependencyModel1.getTenacityConfiguration(),
                UUID.randomUUID().toString(),
                dependencyModel1.getServiceId());
        assertThat(dependencyDB.insert(dependencyModel2)).isEqualTo(1);
        assertThat(dependencyDB.all(dependencyModel1.getDependencyId(), dependencyModel1.getServiceId()))
                .containsOnly(dependencyModel1, dependencyModel2);
        assertThat(dependencyDB.insert(dependencyModel())).isEqualTo(1);
        assertThat(dependencyDB.all(dependencyModel1.getDependencyId(), dependencyModel1.getServiceId()))
                .containsOnly(dependencyModel1, dependencyModel2);
    }
}