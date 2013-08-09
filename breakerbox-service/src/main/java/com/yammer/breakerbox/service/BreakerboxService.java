package com.yammer.breakerbox.service;

import com.netflix.turbine.init.TurbineInit;
import com.netflix.turbine.streaming.servlet.TurbineStreamServlet;
import com.yammer.breakerbox.service.config.BreakerboxConfiguration;
import com.yammer.breakerbox.service.resources.ConfigurationResource;
import com.yammer.breakerbox.service.resources.ConfigureResource;
import com.yammer.breakerbox.service.resources.DashboardResource;
import com.yammer.breakerbox.service.tenacity.BreakerboxDependencyKey;
import com.yammer.breakerbox.service.tenacity.ScheduledTenacityPoller;
import com.yammer.breakerbox.service.tenacity.TenacityPoller;
import com.yammer.breakerbox.service.tenacity.TenacityPropertyKeysStore;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.client.TenacityClientFactory;
import com.yammer.tenacity.core.bundle.TenacityBundle;
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
        bootstrap.addBundle(new TenacityBundle(BreakerboxDependencyKey.values()));
        bootstrap.addBundle(new TenacityDashboardBundle());

        TurbineInit.init();
    }

    @Override
    public void run(BreakerboxConfiguration configuration, Environment environment) throws Exception {
        environment.addServlet(new TurbineStreamServlet(), "/turbine.stream");

        environment.addResource(new ConfigurationResource());
        environment.addResource(new ConfigureResource());
        environment.addResource(new DashboardResource());

        final TenacityPropertyKeysStore tenacityPropertyKeysStore = new TenacityPropertyKeysStore(
                new TenacityPoller.Factory(
                        new TenacityClientFactory(configuration.getTenacityClient())
                                .build(environment)));

        environment.managedScheduledExecutorService("scheduled-tenacity-poller-%d", 1)
                .scheduleAtFixedRate(
                        new ScheduledTenacityPoller(tenacityPropertyKeysStore),
                        0,
                        1,
                        TimeUnit.MINUTES);
    }
}