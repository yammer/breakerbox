package com.yammer.breakerbox.service.archaius;

import com.netflix.config.FixedDelayPollingScheduler;
import com.netflix.config.PolledConfigurationSource;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenacityPollingScheduler extends FixedDelayPollingScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TenacityPollingScheduler.class);

    public TenacityPollingScheduler(int initialDelayMillis, int delayMillis, boolean ignoreDeletesFromSource) {
        super(initialDelayMillis, delayMillis, ignoreDeletesFromSource);
    }

    public TenacityPollingScheduler() {
    }

    @Override
    protected synchronized void initialLoad(PolledConfigurationSource source, Configuration config) {
        try {
            super.initialLoad(source, config);
        } catch (Exception err) {
            LOGGER.warn("Initial dynamic configuration load failed", err);
        }
    }
}
