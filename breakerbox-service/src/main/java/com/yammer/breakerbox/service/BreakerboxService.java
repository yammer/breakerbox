package com.yammer.breakerbox.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.netflix.turbine.init.TurbineInit;
import com.netflix.turbine.streaming.servlet.TurbineStreamServlet;
import com.yammer.breakerbox.azure.AzureStore;
import com.yammer.breakerbox.dashboard.bundle.BreakerboxDashboardBundle;
import com.yammer.breakerbox.jdbi.JdbiConfiguration;
import com.yammer.breakerbox.jdbi.JdbiStore;
import com.yammer.breakerbox.service.auth.NullAuthProvider;
import com.yammer.breakerbox.service.auth.NullAuthenticator;
import com.yammer.breakerbox.service.config.BreakerboxServiceConfiguration;
import com.yammer.breakerbox.service.core.SyncComparator;
import com.yammer.breakerbox.service.resources.ArchaiusResource;
import com.yammer.breakerbox.service.resources.ConfigureResource;
import com.yammer.breakerbox.service.resources.DashboardResource;
import com.yammer.breakerbox.service.resources.InSyncResource;
import com.yammer.breakerbox.service.store.ScheduledTenacityPoller;
import com.yammer.breakerbox.service.store.TenacityPropertyKeysStore;
import com.yammer.breakerbox.service.tenacity.BreakerboxDependencyKey;
import com.yammer.breakerbox.service.tenacity.BreakerboxDependencyKeyFactory;
import com.yammer.breakerbox.service.tenacity.TenacityConfigurationFetcher;
import com.yammer.breakerbox.service.tenacity.TenacityPoller;
import com.yammer.breakerbox.store.BreakerboxStore;
import com.yammer.dropwizard.authenticator.LdapAuthenticator;
import com.yammer.dropwizard.authenticator.LdapCanAuthenticate;
import com.yammer.dropwizard.authenticator.LdapConfiguration;
import com.yammer.dropwizard.authenticator.ResourceAuthenticator;
import com.yammer.dropwizard.authenticator.healthchecks.LdapHealthCheck;
import com.yammer.tenacity.client.TenacityClient;
import com.yammer.tenacity.client.TenacityClientFactory;
import com.yammer.tenacity.core.auth.TenacityAuthenticator;
import com.yammer.tenacity.core.bundle.TenacityBundleBuilder;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.logging.DefaultExceptionLogger;
import com.yammer.tenacity.core.logging.ExceptionLoggingCommandHook;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyRegister;
import com.yammer.tenacity.dbi.DBIExceptionLogger;
import com.yammer.tenacity.dbi.SQLExceptionLogger;
import io.dropwizard.Application;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.auth.basic.BasicAuthProvider;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.bundles.DBIExceptionsBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BreakerboxService extends Application<BreakerboxServiceConfiguration> {
    public static void main(String[] args) throws Exception {
        new BreakerboxService().run(args);
    }

    private BreakerboxService() {}

    @Override
    public void initialize(Bootstrap<BreakerboxServiceConfiguration> bootstrap) {
        bootstrap.addBundle(new DBIExceptionsBundle());
        bootstrap.addBundle(new MigrationsBundle<BreakerboxServiceConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(BreakerboxServiceConfiguration configuration) {
                return configuration.getJdbiConfiguration().or(new JdbiConfiguration()).getDataSourceFactory();
            }
        });
        bootstrap.addBundle(TenacityBundleBuilder
                .newBuilder()
                .propertyKeyFactory(new BreakerboxDependencyKeyFactory())
                .propertyKeys(BreakerboxDependencyKey.values())
                .mapAllHystrixRuntimeExceptionsTo(429)
                .commandExecutionHook(new ExceptionLoggingCommandHook(
                        ImmutableList.of(
                            new DBIExceptionLogger(bootstrap.getMetricRegistry()),
                            new SQLExceptionLogger(bootstrap.getMetricRegistry()),
                            new DefaultExceptionLogger())))
                .build());
        bootstrap.addBundle(new BreakerboxDashboardBundle());
    }
    
    private static void registerProperties(BreakerboxServiceConfiguration configuration) {
        new TenacityPropertyRegister(ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(
                BreakerboxDependencyKey.BRKRBX_SERVICES_PROPERTYKEYS, configuration.getBreakerboxServicesPropertyKeys(),
                BreakerboxDependencyKey.BRKRBX_SERVICES_CONFIGURATION, configuration.getBreakerboxServicesConfiguration(),
                BreakerboxDependencyKey.BRKRBX_LDAP_AUTH, new TenacityConfiguration()),
                configuration.getBreakerboxConfiguration())
                .register();
    }

    @Override
    public void run(final BreakerboxServiceConfiguration configuration, Environment environment) throws Exception {
        setupAuth(configuration, environment);

        final BreakerboxStore breakerboxStore = createBreakerboxStore(configuration, environment);
        breakerboxStore.initialize();

        final TenacityClient tenacityClient = new TenacityClientFactory(configuration.getTenacityClient()).build(environment);
        final TenacityPropertyKeysStore tenacityPropertyKeysStore = new TenacityPropertyKeysStore(
                new TenacityPoller.Factory(tenacityClient));
        final SyncComparator syncComparator = new SyncComparator(
                new TenacityConfigurationFetcher.Factory(tenacityClient),
                breakerboxStore);

        environment.servlets().addServlet("turbine.stream", new TurbineStreamServlet()).addMapping("/turbine.stream");

        environment.jersey().register(new ArchaiusResource(configuration.getArchaiusOverride(), breakerboxStore));
        environment.jersey().register(new ConfigureResource(breakerboxStore, tenacityPropertyKeysStore, syncComparator));
        environment.jersey().register(new DashboardResource());
        environment.jersey().register(new InSyncResource(syncComparator, tenacityPropertyKeysStore));

        final ScheduledExecutorService scheduledExecutorService = environment
                .lifecycle()
                .scheduledExecutorService("scheduled-tenacity-poller-%d")
                .threads(1)
                .build();
        scheduledExecutorService.scheduleAtFixedRate(
                new ScheduledTenacityPoller(tenacityPropertyKeysStore),
                30,
                60,
                TimeUnit.SECONDS);

        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                TurbineInit.init();
                registerProperties(configuration);
            }
        }, 3, TimeUnit.SECONDS);
    }

    private static BreakerboxStore createBreakerboxStore(BreakerboxServiceConfiguration configuration, Environment environment) throws Exception {
        if (configuration.getJdbiConfiguration().isPresent()) {
            return new JdbiStore(configuration.getJdbiConfiguration().get(), environment);
        } else if (configuration.getAzure().isPresent()) {
            return new AzureStore(configuration.getAzure().get(), environment);
        } else {
            throw new IllegalStateException("A datastore must be specified: either azure or database");
        }
    }

    private static void setupAuth(BreakerboxServiceConfiguration configuration, Environment environment) {
        if (configuration.getLdapConfiguration().isPresent()) {
            setupLdapAuth(configuration.getLdapConfiguration().get(), environment);
        } else {
            setupNullAuth(environment);
        }
    }

    private static void setupNullAuth(Environment environment) {
        environment.jersey().register(new NullAuthProvider<>(new NullAuthenticator()));
    }

    private static void setupLdapAuth(LdapConfiguration ldapConfiguration, Environment environment) {
        final LdapAuthenticator ldapAuthenticator = new LdapAuthenticator(ldapConfiguration);
        final ResourceAuthenticator canAuthenticate = new ResourceAuthenticator(
                new LdapCanAuthenticate(ldapConfiguration));
        final CachingAuthenticator<BasicCredentials, BasicCredentials> cachingAuthenticator =
                new CachingAuthenticator<>(
                        environment.metrics(),
                        TenacityAuthenticator.wrap(
                                new ResourceAuthenticator(ldapAuthenticator), BreakerboxDependencyKey.BRKRBX_LDAP_AUTH),
                        ldapConfiguration.getCachePolicy()
                );

        environment.healthChecks().register("ldap-auth", new LdapHealthCheck<>(TenacityAuthenticator
                .wrap(canAuthenticate, BreakerboxDependencyKey.BRKRBX_LDAP_AUTH)));
        environment.jersey().register(new BasicAuthProvider<>(cachingAuthenticator, "breakerbox"));
    }
}