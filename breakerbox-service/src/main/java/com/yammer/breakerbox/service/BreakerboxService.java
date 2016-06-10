package com.yammer.breakerbox.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.netflix.turbine.init.TurbineInit;
import com.netflix.turbine.plugins.PluginsFactory;
import com.netflix.turbine.streaming.servlet.TurbineStreamServlet;
import com.yammer.breakerbox.azure.AzureStore;
import com.yammer.breakerbox.dashboard.bundle.BreakerboxDashboardBundle;
import com.yammer.breakerbox.jdbi.JdbiConfiguration;
import com.yammer.breakerbox.jdbi.JdbiStore;
import com.yammer.breakerbox.service.auth.NullAuthFilter;
import com.yammer.breakerbox.service.auth.NullAuthenticator;
import com.yammer.breakerbox.service.config.BreakerboxServiceConfiguration;
import com.yammer.breakerbox.service.core.SyncComparator;
import com.yammer.breakerbox.service.managed.ManagedTurbine;
import com.yammer.breakerbox.service.resources.*;
import com.yammer.breakerbox.service.store.ScheduledTenacityPoller;
import com.yammer.breakerbox.service.store.TenacityPropertyKeysStore;
import com.yammer.breakerbox.service.tenacity.*;
import com.yammer.breakerbox.service.turbine.LodbrokInstanceDiscovery;
import com.yammer.breakerbox.service.turbine.client.DelegatingLodbrokTenacityClient;
import com.yammer.breakerbox.service.turbine.client.LodbrokTenacityClientBuilder;
import com.yammer.breakerbox.store.BreakerboxStore;
import com.yammer.dropwizard.authenticator.LdapAuthenticator;
import com.yammer.dropwizard.authenticator.LdapConfiguration;
import com.yammer.dropwizard.authenticator.ResourceAuthenticator;
import com.yammer.dropwizard.authenticator.User;
import com.yammer.lodbrok.discovery.core.client.LodbrokClientFactory;
import com.yammer.lodbrok.discovery.core.config.LodbrokDiscoveryConfiguration;
import com.yammer.lodbrok.discovery.core.store.LodbrokInstanceStore;
import com.yammer.lodbrok.discovery.core.store.LodbrokInstanceStorePoller;
import com.yammer.metrics.reporters.chute.graphite.ChuteGraphite;
import com.yammer.metrics.reporters.chute.graphite.ChuteGraphiteConfiguration;
import com.yammer.metrics.reporters.chute.graphite.ChuteGraphiteFactory;
import com.yammer.metrics.reporters.chute.graphite.ChuteGraphiteReporter;
import com.yammer.tenacity.core.auth.TenacityAuthenticator;
import com.yammer.tenacity.core.bundle.TenacityBundleConfigurationFactory;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.logging.DefaultExceptionLogger;
import com.yammer.tenacity.core.logging.ExceptionLoggingCommandHook;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;
import com.yammer.tenacity.dbi.DBIExceptionLogger;
import com.yammer.tenacity.dbi.SQLExceptionLogger;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.bundles.DBIExceptionsBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BreakerboxService extends Application<BreakerboxServiceConfiguration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BreakerboxService.class);
    private DelayedTenacityConfiguredBundle tenacityConfiguredBundle;

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

        tenacityConfiguredBundle = ((DelayedTenacityBundleBuilder)DelayedTenacityBundleBuilder
            .newBuilder()
            .configurationFactory(new TenacityBundleConfigurationFactory<BreakerboxServiceConfiguration>() {
                @Override
                public Map<TenacityPropertyKey, TenacityConfiguration> getTenacityConfigurations(BreakerboxServiceConfiguration applicationConfiguration) {
                    return ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(
                            BreakerboxDependencyKey.BRKRBX_SERVICES_PROPERTYKEYS, applicationConfiguration.getBreakerboxServicesPropertyKeys(),
                            BreakerboxDependencyKey.BRKRBX_SERVICES_CONFIGURATION, applicationConfiguration.getBreakerboxServicesConfiguration(),
                            BreakerboxDependencyKey.BRKRBX_LDAP_AUTH, new TenacityConfiguration());
                }

                @Override
                public TenacityPropertyKeyFactory getTenacityPropertyKeyFactory(BreakerboxServiceConfiguration applicationConfiguration) {
                    return new BreakerboxDependencyKeyFactory();
                }

                @Override
                public BreakerboxConfiguration getBreakerboxConfiguration(BreakerboxServiceConfiguration applicationConfiguration) {
                    return applicationConfiguration.getBreakerboxConfiguration();
                }
            })
            .mapAllHystrixRuntimeExceptionsTo(429)
            .commandExecutionHook(new ExceptionLoggingCommandHook(
                    ImmutableList.of(
                            new DBIExceptionLogger(bootstrap.getMetricRegistry()),
                            new SQLExceptionLogger(bootstrap.getMetricRegistry()),
                            new DefaultExceptionLogger()))))
            .build();
        bootstrap.addBundle(tenacityConfiguredBundle);
        bootstrap.addBundle(new BreakerboxDashboardBundle());
    }

    @Override
    public void run(final BreakerboxServiceConfiguration configuration, final Environment environment) throws Exception {
        registerChuteReporter(configuration.getChute(), environment);
        setupLodbrokInstanceDiscovery(configuration.getLodbrok(), environment);
        setupAuth(configuration, environment);

        final BreakerboxStore breakerboxStore = createBreakerboxStore(configuration, environment);
        breakerboxStore.initialize();

        final TenacityPropertyKeysStore tenacityPropertyKeysStore = new TenacityPropertyKeysStore(
            new TenacityPoller.Factory(new DelegatingLodbrokTenacityClient(
                new LodbrokTenacityClientBuilder(environment, BreakerboxDependencyKey.BRKRBX_SERVICES_PROPERTYKEYS)
                        .using(configuration.getTenacityClient())
                        .build())));
        final SyncComparator syncComparator = new SyncComparator(
            new TenacityConfigurationFetcher.Factory(new DelegatingLodbrokTenacityClient(
                new LodbrokTenacityClientBuilder(environment, BreakerboxDependencyKey.BRKRBX_SERVICES_CONFIGURATION)
                        .using(configuration.getTenacityClient())
                        .build())),
            breakerboxStore);

        final Set<String> metaClusters = configuration
                .getMetaClusters()
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        environment.servlets().addServlet("turbine.stream", new TurbineStreamServlet()).addMapping("/turbine.stream");

        environment.jersey().register(new ArchaiusResource(configuration.getArchaiusOverride(), breakerboxStore));
        environment.jersey().register(new ConfigureResource(breakerboxStore));
        environment.jersey().register(new DashboardResource(configuration.getDefaultDashboard(), configuration.getBreakerboxHostAndPort(), metaClusters));
        environment.jersey().register(new InSyncResource(syncComparator, tenacityPropertyKeysStore));
        environment.jersey().register(new ClustersResource(metaClusters, breakerboxStore, tenacityPropertyKeysStore));

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

        environment.lifecycle().manage(new ManagedTurbine());

        scheduledExecutorService.schedule(() -> {
                //TODO: The way properties get registered shouldn't need to depend on this strict of ordering.
                //Need to also move off the static properties file for Turbine configuration and move to something more
                //dynamic
                if (tenacityConfiguredBundle != null) {
                    tenacityConfiguredBundle.delayedRegisterTenacityProperties(
                            tenacityConfiguredBundle.getTenacityBundleConfigurationFactory().getTenacityConfigurations(configuration),
                            configuration);
                    TurbineInit.init();
                    tenacityConfiguredBundle = null;
                } else {
                    LOGGER.error("Unable to initialize Tenacity/Turbine.");
                    throw new RuntimeException("Unable to initialize Tenacity/Turbine.");
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
        environment.jersey().register(new AuthDynamicFeature(
                        new NullAuthFilter.Builder<User>()
                        .setAuthenticator(new NullAuthenticator())
                        .setRealm("null")
                        .buildAuthFilter()));
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
    }

    private static void setupLdapAuth(LdapConfiguration ldapConfiguration, Environment environment) {
        final LdapAuthenticator ldapAuthenticator = new LdapAuthenticator(ldapConfiguration);
        final CachingAuthenticator<BasicCredentials, User> cachingAuthenticator =
                new CachingAuthenticator<>(
                        environment.metrics(),
                        TenacityAuthenticator.wrap(
                                new ResourceAuthenticator(ldapAuthenticator), BreakerboxDependencyKey.BRKRBX_LDAP_AUTH),
                        ldapConfiguration.getCachePolicy()
                );
        environment.jersey().register(new AuthDynamicFeature(
                        new BasicCredentialAuthFilter.Builder<User>()
                                .setAuthenticator(cachingAuthenticator)
                                .setRealm("breakerbox")
                                .buildAuthFilter()));
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
    }

    private static void setupLodbrokInstanceDiscovery(LodbrokDiscoveryConfiguration configuration,
                                                      Environment environment) {
        final LodbrokClientFactory lodbrokClientFactory = new LodbrokClientFactory(configuration, environment);
        final LodbrokInstanceStore lodbrokInstanceStore = LodbrokInstanceStore.empty();
        final LodbrokInstanceStorePoller lodbrokInstanceStorePoller = LodbrokInstanceStorePoller.build(
                environment,
                lodbrokInstanceStore,
                lodbrokClientFactory.build("lodbrok-client"),
                configuration.getPollInterval());
        lodbrokInstanceStorePoller.schedule();
        PluginsFactory.setInstanceDiscovery(new LodbrokInstanceDiscovery(lodbrokInstanceStore, configuration.getLodbrokUri()));
    }

    private static void registerChuteReporter(ChuteGraphiteConfiguration configuration, Environment environment) {
        final ChuteGraphite chuteGraphite = new ChuteGraphiteFactory(configuration).build(environment);
        final ChuteGraphiteReporter chuteGraphiteReporter = ChuteGraphiteReporter
                .forRegistry(environment.metrics())
                .build(chuteGraphite);
        chuteGraphiteReporter.start(1, TimeUnit.MINUTES);
    }
}