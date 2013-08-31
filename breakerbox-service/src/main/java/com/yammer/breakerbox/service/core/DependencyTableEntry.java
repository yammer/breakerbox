package com.yammer.breakerbox.service.core;

import com.yammer.tenacity.core.config.TenacityConfiguration;

public class DependencyTableEntry {
    private final long timestamp;
    private final String user;
    private final TenacityConfiguration configuration;

    private DependencyTableEntry(long timestamp, String user, TenacityConfiguration configuration) {
        this.timestamp = timestamp;
        this.user = user;
        this.configuration = configuration;
    }

    public static DependencyTableEntry createDefaultConfiguration(long testTimeStamp, String user) {
        return new DependencyTableEntry(testTimeStamp, user, new TenacityConfiguration());
    }

    public static DependencyTableEntry createDefaultConfiguration(String user, TenacityConfiguration configuration1) {
        return new DependencyTableEntry(System.currentTimeMillis(), user, configuration1);
    }

    public static DependencyTableEntry create(Long timestamp, String user, TenacityConfiguration dependencyConfiguration) {
        return new DependencyTableEntry(timestamp, user, dependencyConfiguration);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUser() {
        return user;
    }

    public TenacityConfiguration getConfiguration() {
        return configuration;
    }
}
