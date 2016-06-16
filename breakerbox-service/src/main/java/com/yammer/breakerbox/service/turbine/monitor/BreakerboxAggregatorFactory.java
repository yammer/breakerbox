package com.yammer.breakerbox.service.turbine.monitor;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.turbine.data.AggDataFromCluster;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;
import com.netflix.turbine.handler.PerformanceCriteria;
import com.netflix.turbine.handler.TurbineDataHandler;
import com.netflix.turbine.monitor.TurbineDataMonitor;
import com.netflix.turbine.monitor.cluster.BreakerboxAggregateClusterMonitor;
import com.netflix.turbine.monitor.cluster.ClusterMonitor;
import com.netflix.turbine.monitor.cluster.ClusterMonitorFactory;
import com.netflix.turbine.plugins.DefaultAggregatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BreakerboxAggregatorFactory implements ClusterMonitorFactory<AggDataFromCluster> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultAggregatorFactory.class);

    // config
    private static final DynamicStringProperty aggClusters = DynamicPropertyFactory.getInstance().getStringProperty(InstanceDiscovery.TURBINE_AGGREGATOR_CLUSTER_CONFIG, null);

    /**
     * @return {@link ClusterMonitor}<{@link AggDataFromCluster}>
     */
    @Override
    public ClusterMonitor<AggDataFromCluster> getClusterMonitor(String name) {
        TurbineDataMonitor<AggDataFromCluster> clusterMonitor = BreakerboxAggregateClusterMonitor.AggregatorClusterMonitorConsole.findMonitor(name + "_agg");
        return (ClusterMonitor<AggDataFromCluster>) clusterMonitor;
    }

    /**
     * Inits all configured cluster monitors
     */
    @Override
    public void initClusterMonitors() {

        for(String clusterName : getClusterNames()) {
            ClusterMonitor<AggDataFromCluster> clusterMonitor = (ClusterMonitor<AggDataFromCluster>) BreakerboxAggregateClusterMonitor.findOrRegisterAggregateMonitor(clusterName);
            clusterMonitor.registerListenertoClusterMonitor(StaticListener);
            try {
                clusterMonitor.startMonitor();
            } catch (Exception e) {
                logger.warn("Could not init cluster monitor for: " + clusterName);
                clusterMonitor.stopMonitor();
                clusterMonitor.getDispatcher().stopDispatcher();
            }
        }
    }

    /**
     * shutdown all configured cluster monitors
     */
    @Override
    public void shutdownClusterMonitors() {

        for(String clusterName : getClusterNames()) {
            ClusterMonitor<AggDataFromCluster> clusterMonitor = (ClusterMonitor<AggDataFromCluster>) BreakerboxAggregateClusterMonitor.findOrRegisterAggregateMonitor(clusterName);
            clusterMonitor.stopMonitor();
            clusterMonitor.getDispatcher().stopDispatcher();
        }
    }

    private List<String> getClusterNames() {

        List<String> clusters = new ArrayList<String>();
        String clusterNames = aggClusters.get();
        if (clusterNames == null || clusterNames.trim().length() == 0) {
            clusters.add("default");
        } else {
            String[] parts = aggClusters.get().split(",");
            for (String s : parts) {
                clusters.add(s);
            }
        }
        return clusters;
    }
    private TurbineDataHandler<AggDataFromCluster> StaticListener = new TurbineDataHandler<AggDataFromCluster>() {

        @Override
        public String getName() {
            return "StaticListener_For_Aggregator";
        }

        @Override
        public void handleData(Collection<AggDataFromCluster> stats) {
        }

        @Override
        public void handleHostLost(Instance host) {
        }

        @Override
        public PerformanceCriteria getCriteria() {
            return NonCriticalCriteria;
        }

    };

    private PerformanceCriteria NonCriticalCriteria = new PerformanceCriteria() {

        @Override
        public boolean isCritical() {
            return false;
        }

        @Override
        public int getMaxQueueSize() {
            return 0;
        }

        @Override
        public int numThreads() {
            return 0;
        }
    };
}
