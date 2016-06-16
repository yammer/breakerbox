package com.yammer.breakerbox.service.turbine.monitor;

import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.turbine.data.AggDataFromCluster;
import com.netflix.turbine.data.DataFromSingleInstance;
import com.netflix.turbine.data.TurbineData;
import com.netflix.turbine.data.meta.MetaInfoUpdator;
import com.netflix.turbine.data.meta.MetaInformation;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceObservable;
import com.netflix.turbine.handler.TurbineDataDispatcher;
import com.netflix.turbine.handler.TurbineDataHandler;
import com.netflix.turbine.monitor.MonitorConsole;
import com.netflix.turbine.monitor.TurbineDataMonitor;
import com.netflix.turbine.monitor.cluster.ClusterMonitor;
import com.netflix.turbine.monitor.cluster.ObservationCriteria;
import com.netflix.turbine.monitor.instance.InstanceMonitor;
import com.netflix.turbine.monitor.instance.InstanceUrlClosure;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public abstract class BreakerboxClusterMonitor<K extends TurbineData> extends ClusterMonitor<K> {

    private static final Logger logger = LoggerFactory.getLogger(ClusterMonitor.class);

    protected final InstanceObservable.InstanceObserver breakerboxMonitorManager;

    private final AtomicInteger hostCount = new AtomicInteger(0);

    /**
     * @param name
     * @param clusterDispatcher : the dispatcher to dispatch cluster events to - e.g aggregated events
     * @param clusterConsole    : the console to register itself with, so that it can be discoverd by other listeners to cluster data
     * @param hostDispatcher    : the dispatcher to receive host events from
     * @param hostConsole       : the host console to maintain host connections in.
     * @param urlClosure        : the config dictating how to connect to a host.
     */
    public BreakerboxClusterMonitor(String name,
                          TurbineDataDispatcher<K> clusterDispatcher, MonitorConsole<K> clusterConsole,
                          TurbineDataDispatcher<DataFromSingleInstance> hostDispatcher, MonitorConsole<DataFromSingleInstance> hostConsole,
                          InstanceUrlClosure urlClosure) {
        this(name,
                clusterDispatcher, clusterConsole,
                hostDispatcher, hostConsole,
                urlClosure,
                InstanceObservable.getInstance());
    }

    public BreakerboxClusterMonitor(String name, TurbineDataDispatcher<K> cDispatcher, MonitorConsole<K> cConsole, TurbineDataDispatcher<DataFromSingleInstance> hDispatcher, MonitorConsole<DataFromSingleInstance> hConsole, InstanceUrlClosure urlClosure, InstanceObservable instanceObservable) {
        super(name, cDispatcher, cConsole, hDispatcher, hConsole, urlClosure, instanceObservable);
        this.breakerboxMonitorManager = new BreakerboxClusterMonitorInstanceManager();
    }

    @Monitor(name="hostCount", type=DataSourceType.GAUGE)
    public int getHostCount() {
        return hostCount.get();
    }

    /**
     * Start the monitor and register with the InstanceObservable to get updates on host status
     * @throws Exception
     */
    @Override
    public void startMonitor() throws Exception {
        // start up the monitor workers from here and register the event handlers
        logger.info("Starting up the cluster monitor for " + name);
        instanceObservable.register(breakerboxMonitorManager);

        MetaInformation<K> metaInfo = getMetaInformation();
        if (metaInfo != null) {
            MetaInfoUpdator.addMetaInfo(metaInfo);
        }
    }

    /**
     * Stop the monitor, shut down resources that were created and notify listeners downstream about the event.
     */
    @Override
    public void stopMonitor() {
        logger.info("Stopping cluster monitor for " + name);
        stopped = true;
        // remove my event handler from all host monitors
        instanceObservable.deregister(breakerboxMonitorManager);
        // notify people below me that this monitor is shutting down
        clusterDispatcher.handleHostLost(getStatsInstance());
        // remove the handler from the host level dispatcher
        hostDispatcher.deregisterEventHandler(getEventHandler());
        // remove this monitor from the StatsEventConsole
        clusterConsole.removeMonitor(getName());

        clusterDispatcher.stopDispatcher();

        MetaInformation<K> metaInfo = getMetaInformation();
        if (metaInfo != null) {
            MetaInfoUpdator.removeMetaInfo(metaInfo);
        }

        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.unregisterMBean(new ObjectName("ClusterMonitorMBean:name=ClusterMonitorStats_" + name));
        } catch (Exception e) {
        }
    }

    /**
     * Helper class that responds to hostup and hostdown events and thus can start / stop InstanceMonitors
     *
     */
    public class BreakerboxClusterMonitorInstanceManager implements InstanceObservable.InstanceObserver {
        @Override
        public String getName() {
            return name;
        }

        public void hostUp(Instance host) {

            if (!(getObservationCriteria().observeHost(host))) {
                return;
            }

            TurbineDataMonitor<DataFromSingleInstance> monitor = getMonitor(host);
            try {
                if (hostDispatcher.findHandlerForHost(host, getEventHandler().getName()) == null) {
                    // this handler is not already present for this host, then add it
                    hostDispatcher.registerEventHandler(host, getEventHandler());
                }
                monitor.startMonitor();
            } catch (Throwable t) {
                logger.info("Failed to start monitor: " + monitor.getName() + ", ex message: ", t);
                monitor.stopMonitor();
                logger.info("Removing monitor from stats event console");
                TurbineDataMonitor<DataFromSingleInstance> oldMonitor = hostConsole.removeMonitor(monitor.getName());
                if (oldMonitor != null) {
                    hostCount.decrementAndGet();
                }
            }
        }

        public void hostDown(Instance host) {
            TurbineDataMonitor<DataFromSingleInstance> hostMonitor = hostConsole.findMonitor(host.getHostname());
            if (hostMonitor != null) {
                hostCount.decrementAndGet();
                hostMonitor.stopMonitor();
                logger.info("Removing monitor from stats event console");
                hostConsole.removeMonitor(hostMonitor.getName());
            }
        }

        private TurbineDataMonitor<DataFromSingleInstance> getMonitor(Instance host) {

            TurbineDataMonitor<DataFromSingleInstance> monitor = hostConsole.findMonitor(host.getHostname());
            if (monitor == null) {
                monitor = new BreakerboxInstanceMonitor(host, urlClosure, hostDispatcher, hostConsole);
                hostCount.incrementAndGet();
                return hostConsole.findOrRegisterMonitor(monitor);
            } else {
                return monitor;
            }
        }

        @Override
        public void hostsUp(Collection<Instance> hosts) {
            for (Instance host : hosts) {
                try {
                    hostUp(host);
                } catch (Throwable t) {
                    logger.error("Could not start monitor on hostUp: " + host.toString(), t);
                }
            }
        }

        @Override
        public void hostsDown(Collection<Instance> hosts) {
            for (Instance host : hosts) {
                try {
                    hostDown(host);
                } catch (Throwable t) {
                    logger.error("Could not stop monitor on hostDown: " + host.toString(), t);
                }
            }
        }
    }


    @RunWith(MockitoJUnitRunner.class)
    public static class UnitTest {

        // the cluster related stuff
        @Mock
        private TurbineDataDispatcher<AggDataFromCluster> cDispatcher;
        @Mock private MonitorConsole<AggDataFromCluster> cConsole;

        // the host monitor related stuff
        @Mock private TurbineDataDispatcher<DataFromSingleInstance> hDispatcher;
        @Mock private MonitorConsole<DataFromSingleInstance> hConsole;

        @Mock private InstanceObservable iObservable;
        protected InstanceUrlClosure testUrlClosure = new InstanceUrlClosure() {
            @Override
            public String getUrlPath(Instance host) {
                return "";
            }
        };

        @Mock private TurbineDataHandler<DataFromSingleInstance> handler;
        @Mock private ObservationCriteria mCriteria;

        @Test
        public void testCleanStartupAndShutdown() throws Exception {

            TestClusterMonitor monitor = new TestClusterMonitor();

            monitor.startMonitor();

            verify(iObservable).register(monitor.breakerboxMonitorManager);

            monitor.stopMonitor();

            verify(iObservable).deregister(monitor.breakerboxMonitorManager);
            verify(cDispatcher).handleHostLost(monitor.statsInstance);
            verify(cDispatcher).stopDispatcher();
            verify(hDispatcher).deregisterEventHandler(handler);
            verify(cConsole).removeMonitor(monitor.getName());
        }

        @Test
        public void testHostUp() throws Exception {

            InstanceMonitor hostMon = mock(InstanceMonitor.class);
            when(hConsole.findMonitor(any(String.class))).thenReturn(hostMon);

            when(mCriteria.observeHost(any(Instance.class))).thenReturn(true);

            TestClusterMonitor monitor = new TestClusterMonitor();

            Instance host1 = new Instance("testHost1", "testCluster", true);

            monitor.breakerboxMonitorManager.hostsUp(Collections.singletonList(host1));

            verify(hConsole).findMonitor(host1.getHostname());
            verify(hostMon).startMonitor();
            verify(hDispatcher).registerEventHandler(host1, handler);
        }

        @Test
        public void testHostDown() throws Exception {

            Instance host1 = new Instance("testHost1", "testCluster", false);

            InstanceMonitor hostMon = mock(InstanceMonitor.class);
            when(hConsole.findMonitor(any(String.class))).thenReturn(hostMon);
            when(hostMon.getName()).thenReturn(host1.getHostname());

            TestClusterMonitor monitor = new TestClusterMonitor();


            monitor.breakerboxMonitorManager.hostsDown(Collections.singletonList(host1));

            verify(hConsole).findMonitor(host1.getHostname());
            verify(hostMon).stopMonitor();
            verify(hConsole).removeMonitor("testHost1");
        }

        private class TestClusterMonitor extends BreakerboxClusterMonitor<AggDataFromCluster> {

            public TestClusterMonitor() {
                super("testMonitor", cDispatcher, cConsole, hDispatcher, hConsole, testUrlClosure, iObservable);
            }

            @Override
            public TurbineDataHandler<DataFromSingleInstance> getEventHandler() {
                return handler;
            }

            @Override
            public ObservationCriteria getObservationCriteria() {
                return mCriteria;
            }
        }
    }
}
