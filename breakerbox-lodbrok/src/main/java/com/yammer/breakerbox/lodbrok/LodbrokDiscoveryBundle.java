package com.yammer.breakerbox.lodbrok;

import com.netflix.turbine.plugins.PluginsFactory;
import com.yammer.breakerbox.lodbrok.turbine.BreakerboxAggregatorFactory;
import com.yammer.lodbrok.discovery.core.client.LodbrokClientFactory;
import com.yammer.lodbrok.discovery.core.config.LodbrokDiscoveryConfiguration;
import com.yammer.lodbrok.discovery.core.store.LodbrokInstanceStore;
import com.yammer.lodbrok.discovery.core.store.LodbrokInstanceStorePoller;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public abstract class LodbrokDiscoveryBundle<T extends Configuration> implements ConfiguredBundle<T> {
    @Override
    public void run(T configuration, Environment environment) throws Exception {
        final LodbrokDiscoveryConfiguration lodbrokDiscoveryConfiguration = getLodbrokDiscoveryConfiguration(configuration);
        final LodbrokClientFactory lodbrokClientFactory = new LodbrokClientFactory(lodbrokDiscoveryConfiguration, environment);
        final LodbrokInstanceStore lodbrokInstanceStore = LodbrokInstanceStore.empty();
        final LodbrokInstanceStorePoller lodbrokInstanceStorePoller = LodbrokInstanceStorePoller.build(
                environment,
                lodbrokInstanceStore,
                lodbrokClientFactory.build("lodbrok-client"),
                lodbrokDiscoveryConfiguration.getPollInterval());
        lodbrokInstanceStorePoller.schedule();
        PluginsFactory.setClusterMonitorFactory(new BreakerboxAggregatorFactory());
        PluginsFactory.setInstanceDiscovery(new LodbrokInstanceDiscovery(lodbrokInstanceStore, lodbrokDiscoveryConfiguration.getUri()));
    }

    protected abstract LodbrokDiscoveryConfiguration getLodbrokDiscoveryConfiguration(T configuration);

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }
}
