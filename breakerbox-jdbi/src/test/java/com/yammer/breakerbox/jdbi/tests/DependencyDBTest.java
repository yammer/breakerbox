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
}