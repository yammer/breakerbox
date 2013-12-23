package com.yammer.breakerbox.dashboard;

import com.yammer.breakerbox.dashboard.bundle.BreakerboxDashboardBundle;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;

public class BreakerboxDashboardService extends Service<Configuration> {
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
