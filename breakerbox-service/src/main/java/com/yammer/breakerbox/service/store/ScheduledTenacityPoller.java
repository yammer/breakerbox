package com.yammer.breakerbox.service.store;

import com.yammer.breakerbox.service.core.Instances;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class ScheduledTenacityPoller implements Runnable {
    private final TenacityPropertyKeysStore tenacityPropertyKeysStore;
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTenacityPoller.class);

    public ScheduledTenacityPoller(TenacityPropertyKeysStore tenacityPropertyKeysStore) {
        this.tenacityPropertyKeysStore = tenacityPropertyKeysStore;
    }

    @Override
    public void run() {
        try {
            for (URI uri : Instances.propertyKeyUris()) {
                tenacityPropertyKeysStore.getTenacityPropertyKeys(uri);
            }
        } catch (Exception err) {
            LOGGER.warn("Unexpected exception", err);
        }
    }
}