package com.yammer.breakerbox.dashboard;

import com.yammer.breakerbox.dashboard.bundle.BreakerboxDashboardBundle;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class BreakerboxDashboardService extends Application<Configuration> {
    private BreakerboxDashboardService() {}

    public static void main(String[] args) throws Exception {
        new BreakerboxDashboardService().run(args);
    }
    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new BreakerboxDashboardBundle());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
    }
}