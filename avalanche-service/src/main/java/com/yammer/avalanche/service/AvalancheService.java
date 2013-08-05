package com.yammer.avalanche.service;

import com.netflix.turbine.init.TurbineInit;
import com.netflix.turbine.streaming.servlet.TurbineStreamServlet;
import com.yammer.avalanche.service.config.AvalancheConfiguration;
import com.yammer.avalanche.service.resources.SelectionResource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.tenacity.dashboard.bundle.TenacityDashboardBundle;
import com.yammer.avalanche.service.resources.ConfigurationResource;

public class AvalancheService extends Service<AvalancheConfiguration> {
    public static void main(String[] args) throws Exception {
        new AvalancheService().run(args);
    }

    private AvalancheService() {}

    @Override
    public void initialize(Bootstrap<AvalancheConfiguration> bootstrap) {
        bootstrap.setName("Avalanche");
        bootstrap.addBundle(new TenacityDashboardBundle());

        TurbineInit.init();
    }

    @Override
    public void run(AvalancheConfiguration configuration, Environment environment) throws Exception {
        environment.addServlet(new TurbineStreamServlet(), "/turbine.stream");

        environment.addResource(new ConfigurationResource());
        environment.addResource(new SelectionResource());
    }
}