package com.yammer.breakerbox.service.azure;

import com.yammer.tenacity.core.config.TenacityConfiguration;

public class DependencyEntityData {
    private final long timestamp;
    private final String user;
    private final TenacityConfiguration configuration;

    private DependencyEntityData(long timestamp, String user, TenacityConfiguration configuration) {
        this.timestamp = timestamp;
        this.user = user;
        this.configuration = configuration;
    }

    public static DependencyEntityData createDefaultConfiguration(long timeStamp, String user) {
        return new DependencyEntityData(timeStamp, user, new TenacityConfiguration());
    }

    public static DependencyEntityData createDefaultConfiguration(String user, TenacityConfiguration configuration) {
        return new DependencyEntityData(System.currentTimeMillis(), user, configuration);
    }

    public static DependencyEntityData create(Long timestamp, String user, TenacityConfiguration dependencyConfiguration) {
        return new DependencyEntityData(timestamp, user, dependencyConfiguration);
    }

    public static DependencyEntityData createLookup(long timestamp) {
        //user is left blank as it is neither the partition key nor the row. It's just data that we don't need for
        //a columnar lookup.
        return new DependencyEntityData(timestamp, "", new TenacityConfiguration());
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
