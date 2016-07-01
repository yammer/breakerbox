package com.yammer.breakerbox.turbine;

import com.google.common.base.Joiner;
import com.netflix.config.ConfigurationManager;
import com.netflix.turbine.discovery.InstanceDiscovery;
import com.netflix.turbine.monitor.cluster.ClusterMonitorFactory;
import com.netflix.turbine.plugins.PluginsFactory;
import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class TurbineInstanceDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger(TurbineInstanceDiscovery.class);
    public static final String BREAKERBOX_INSTANCE_ID = "Breakerbox-Instance-Id";
    public static final String DEFAULT_URL_SUFFIX = "/tenacity/metrics.stream";

    private TurbineInstanceDiscovery() {}

    public static void registerClusters(Collection<String> clusterNames) {
        registerClusters(clusterNames, DEFAULT_URL_SUFFIX);
    }

    public static void registerClusters(Collection<String> clusterNames,
                                        String instanceUrlSuffix) {
        final AbstractConfiguration configurationManager = ConfigurationManager.getConfigInstance();
        configurationManager.setProperty(InstanceDiscovery.TURBINE_AGGREGATOR_CLUSTER_CONFIG,
                Joiner.on(',').join(clusterNames));
        configurationManager.setProperty("turbine.instanceUrlSuffix", instanceUrlSuffix);
        final ClusterMonitorFactory<?> clusterMonitorFactory = PluginsFactory.getClusterMonitorFactory();
        if (clusterMonitorFactory != null) {
            try {
                clusterMonitorFactory.initClusterMonitors();
            } catch (Exception err) {
                LOGGER.error("Trouble initializing cluster monitors", err);
            }
        }
    }
}
