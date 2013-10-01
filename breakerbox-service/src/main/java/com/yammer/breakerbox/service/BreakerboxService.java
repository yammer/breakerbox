package com.yammer.breakerbox.service;

import com.google.common.collect.ImmutableMap;
import com.netflix.turbine.init.TurbineInit;
import com.netflix.turbine.streaming.servlet.TurbineStreamServlet;
import com.yammer.azure.TableClient;
import com.yammer.azure.TableClientFactory;
import com.yammer.azure.healthchecks.TableClientHealthcheck;
import com.yammer.breakerbox.service.azure.TableId;
import com.yammer.breakerbox.service.config.BreakerboxConfiguration;
import com.yammer.breakerbox.service.config.LdapConfiguration;
import com.yammer.breakerbox.service.core.BreakerboxStore;
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
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.auth.CachingAuthenticator;
import com.yammer.dropwizard.auth.basic.BasicAuthProvider;
import com.yammer.dropwizard.auth.basic.BasicCredentials;
import com.yammer.dropwizard.authenticator.LdapAuthenticator;
import com.yammer.dropwizard.authenticator.healthchecks.LdapHealthCheck;
import com.yammer.dropwizard.authenticator.resources.ResourceAuthenticator;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.client.TenacityClient;
import com.yammer.tenacity.client.TenacityClientFactory;
import com.yammer.tenacity.core.bundle.TenacityBundle;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.ArchaiusPropertyRegister;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyRegister;
import com.yammer.tenacity.dashboard.bundle.TenacityDashboardBundle;

import java.util.concurrent.TimeUnit;

public class BreakerboxService extends Service<BreakerboxConfiguration> {
    public static void main(String[] args) throws Exception {
        new BreakerboxService().run(args);
    }

    private BreakerboxService() {}

    @Override
    public void initialize(Bootstrap<BreakerboxConfiguration> bootstrap) {
        bootstrap.setName("Breakerbox");
        bootstrap.addBundle(new TenacityBundle(new BreakerboxDependencyKeyFactory(), BreakerboxDependencyKey.values()));
        bootstrap.addBundle(new TenacityDashboardBundle());

        TurbineInit.init();
    }
    
    private static void registerProperties(BreakerboxConfiguration configuration) {
        new TenacityPropertyRegister(ImmutableMap.<TenacityPropertyKey, TenacityConfiguration>of(
                BreakerboxDependencyKey.BRKRBX_SERVICES_PROPERTYKEYS, configuration.getBreakerboxServicesPropertyKeys(),
                BreakerboxDependencyKey.BRKRBX_SERVICES_CONFIGURATION, configuration.getBreakerboxServicesConfiguration()),
                configuration.getBreakerboxConfiguration(),
                new ArchaiusPropertyRegister())
                .register();
    }

    private static void initializeAzureTables(TableClient tableClient) {
        for (TableId tableId : TableId.values()) {
            tableClient.create(tableId);
        }
    }

    @Override
    public void run(BreakerboxConfiguration configuration, Environment environment) throws Exception {
        setupAuth(configuration.getLdapConfiguration(), environment);

        final TableClient tableClient = new TableClientFactory(configuration.getAzure()).create();
        final BreakerboxStore breakerboxStore = new BreakerboxStore(tableClient);
        final TenacityClient tenacityClient = new TenacityClientFactory(configuration.getTenacityClient()).build(environment);
        final TenacityPropertyKeysStore tenacityPropertyKeysStore = new TenacityPropertyKeysStore(
                new TenacityPoller.Factory(tenacityClient));
        final SyncComparator syncComparator = new SyncComparator(
                new TenacityConfigurationFetcher.Factory(tenacityClient),
                breakerboxStore);

        initializeAzureTables(tableClient);
        
        environment.addHealthCheck(new TableClientHealthcheck(tableClient));

        environment.addServlet(new TurbineStreamServlet(), "/turbine.stream");

        environment.addResource(new ArchaiusResource(configuration.getArchaiusOverride(), breakerboxStore));
        environment.addResource(new ConfigureResource(breakerboxStore, tenacityPropertyKeysStore, syncComparator));
        environment.addResource(new DashboardResource());
        environment.addResource(new InSyncResource(syncComparator, tenacityPropertyKeysStore));

        registerProperties(configuration);

        environment.managedScheduledExecutorService("scheduled-tenacity-poller-%d", 1)
                .scheduleAtFixedRate(
                        new ScheduledTenacityPoller(tenacityPropertyKeysStore),
                        0,
                        1,
                        TimeUnit.MINUTES);
    }

    private static void setupAuth(LdapConfiguration ldapConfiguration, Environment environment) {
        final LdapAuthenticator ldapAuthenticator = new LdapAuthenticator(ldapConfiguration.getHostAndPort());
        final CachingAuthenticator<BasicCredentials, BasicCredentials> resourceAuthenticator =
                CachingAuthenticator.wrap(new ResourceAuthenticator(ldapAuthenticator), ldapConfiguration.getCache());

        environment.addHealthCheck(new LdapHealthCheck(ldapAuthenticator));
        environment.addResource(new BasicAuthProvider<>(resourceAuthenticator, "breakerbox"));
    }
}
