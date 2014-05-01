package com.yammer.breakerbox.jdbi.tests;

import com.codahale.metrics.MetricRegistry;
import com.yammer.breakerbox.jdbi.JdbiConfiguration;
import com.yammer.breakerbox.jdbi.args.DateTimeArgumentFactory;
import com.yammer.breakerbox.jdbi.args.DependencyIdArgumentFactory;
import com.yammer.breakerbox.jdbi.args.ServiceIdArgumentFactory;
import com.yammer.breakerbox.jdbi.args.TenacityConfigurationArgumentFactory;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.store.model.DependencyModel;
import com.yammer.breakerbox.store.model.ServiceModel;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.migrations.CloseableLiquibase;
import io.dropwizard.setup.Environment;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.skife.jdbi.v2.DBI;

import javax.validation.Validation;
import java.util.UUID;

public class H2Test {
    protected final JdbiConfiguration hsqlConfig = new JdbiConfiguration();
    protected CloseableLiquibase liquibase;
    protected DBI database;

    {
        hsqlConfig.getDataSourceFactory().setUrl("jdbc:h2:mem:DbTest-" + System.currentTimeMillis());
        hsqlConfig.getDataSourceFactory().setUser("sa");
        hsqlConfig.getDataSourceFactory().setDriverClass("org.h2.Driver");
        hsqlConfig.getDataSourceFactory().setValidationQuery("SELECT 1");
    }

    @Before
    public void setupH2Test() throws Exception {
        liquibase = new CloseableLiquibase(hsqlConfig
                .getDataSourceFactory()
                .build(new MetricRegistry(), "liquibase"));
        liquibase.update("");
        database = new DBIFactory().build(environment(), hsqlConfig.getDataSourceFactory(), "h2test");
        database.registerArgumentFactory(new DependencyIdArgumentFactory());
        database.registerArgumentFactory(new ServiceIdArgumentFactory());
        database.registerArgumentFactory(new TenacityConfigurationArgumentFactory(Jackson.newObjectMapper()));
        database.registerArgumentFactory(new DateTimeArgumentFactory());
    }

    @After
    public void teardownH2Test() throws Exception {
        liquibase.dropAll();
    }

    protected static Environment environment() {
        return new Environment(
                "test",
                Jackson.newObjectMapper(),
                Validation.buildDefaultValidatorFactory().getValidator(),
                new MetricRegistry(),
                Thread.currentThread().getContextClassLoader());
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

    protected static DependencyModel dependencyModel(DateTime dateTime) {
        return new DependencyModel(
                DependencyId.from(UUID.randomUUID().toString()),
                dateTime,
                new TenacityConfiguration(),
                UUID.randomUUID().toString(),
                ServiceId.from(UUID.randomUUID().toString()));
    }
}
