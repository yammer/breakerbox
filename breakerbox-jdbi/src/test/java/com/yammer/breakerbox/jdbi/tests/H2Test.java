package com.yammer.breakerbox.jdbi.tests;

import com.yammer.breakerbox.jdbi.args.DependencyIdArgumentFactory;
import com.yammer.breakerbox.jdbi.args.ServiceIdArgumentFactory;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.jdbi.DBIFactory;
import com.yammer.dropwizard.migrations.ManagedLiquibase;
import org.junit.After;
import org.junit.Before;
import org.skife.jdbi.v2.DBI;

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
    }

    @After
    public void teardownH2Test() throws Exception {
        liquibase.dropAll();
    }
}
