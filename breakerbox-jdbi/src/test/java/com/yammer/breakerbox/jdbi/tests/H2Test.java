package com.yammer.breakerbox.jdbi.tests;

import com.yammer.breakerbox.jdbi.args.DateTimeArgumentFactory;
import com.yammer.breakerbox.jdbi.args.DependencyIdArgumentFactory;
import com.yammer.breakerbox.jdbi.args.ServiceIdArgumentFactory;
import com.yammer.breakerbox.jdbi.args.TenacityConfigurationArgumentFactory;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.store.model.DependencyModel;
import com.yammer.breakerbox.store.model.ServiceModel;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.jdbi.DBIFactory;
import com.yammer.dropwizard.migrations.ManagedLiquibase;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.skife.jdbi.v2.DBI;

import java.util.UUID;

import static org.mockito.Mockito.mock;

public class H2Test {
    protected final DatabaseConfiguration hsqlConfig = new DatabaseConfiguration();
    protected ManagedLiquibase liquibase;
    protected DBI database;

    {
        hsqlConfig.setUrl("jdbc:h2:mem:DbTest-" + System.currentTimeMillis());
        hsqlConfig.setUser("sa");
        hsqlConfig.setDriverClass("org.h2.Driver");
        hsqlConfig.setValidationQuery("SELECT 1");
    }

    @Before
    public void setupH2Test() throws Exception {
        liquibase = new ManagedLiquibase(hsqlConfig);
        liquibase.update("");
        database = new DBIFactory().build(mock(Environment.class), hsqlConfig, "h2test");
        database.registerArgumentFactory(new DependencyIdArgumentFactory());
        database.registerArgumentFactory(new ServiceIdArgumentFactory());
        database.registerArgumentFactory(new TenacityConfigurationArgumentFactory());
        database.registerArgumentFactory(new DateTimeArgumentFactory());
    }

    @After
    public void teardownH2Test() throws Exception {
        liquibase.dropAll();
    }

    protected static ServiceModel serviceModel() {
        return new ServiceModel(
                ServiceId.from(UUID.randomUUID().toString()),
                DependencyId.from(UUID.randomUUID().toString()));
    }

    protected static DependencyModel dependencyModel() {
        return new DependencyModel(
                DependencyId.from(UUID.randomUUID().toString()),
                DateTime.now(),
                new TenacityConfiguration(),
                UUID.randomUUID().toString(),
                ServiceId.from(UUID.randomUUID().toString()));
    }
}
