package com.yammer.breakerbox.service;

import com.netflix.turbine.init.TurbineInit;
import com.netflix.turbine.streaming.servlet.TurbineStreamServlet;
import com.yammer.breakerbox.service.config.BreakerboxConfiguration;
import com.yammer.breakerbox.service.resources.ConfigurationResource;
import com.yammer.breakerbox.service.resources.ConfigureResource;
import com.yammer.breakerbox.service.resources.DashboardResource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.dashboard.bundle.TenacityDashboardBundle;

public class BreakerboxService extends Service<BreakerboxConfiguration> {
    public static void main(String[] args) throws Exception {
        new BreakerboxService().run(args);
    }

    private BreakerboxService() {}

    @Override
    public void initialize(Bootstrap<BreakerboxConfiguration> bootstrap) {
        bootstrap.setName("Breakerbox");
        bootstrap.addBundle(new TenacityDashboardBundle());

        TurbineInit.init();
    }

    @Override
    public void run(BreakerboxConfiguration configuration, Environment environment) throws Exception {
        environment.addServlet(new TurbineStreamServlet(), "/turbine.stream");

        environment.addResource(new ConfigurationResource());
        environment.addResource(new ConfigureResource());
        environment.addResource(new DashboardResource());
    }
}