package com.yammer.breakerbox.service.store;

import com.yammer.breakerbox.service.core.Instances;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduledTenacityPoller implements Runnable {
    private final TenacityPropertyKeysStore tenacityPropertyKeysStore;
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTenacityPoller.class);

    public ScheduledTenacityPoller(TenacityPropertyKeysStore tenacityPropertyKeysStore) {
        this.tenacityPropertyKeysStore = tenacityPropertyKeysStore;
    }

    @Override
    public void run() {
        try {
            Instances.instances().forEach(tenacityPropertyKeysStore::getTenacityPropertyKeys);
        } catch (Exception err) {
            LOGGER.warn("Unexpected exception", err);
        }
    }
}