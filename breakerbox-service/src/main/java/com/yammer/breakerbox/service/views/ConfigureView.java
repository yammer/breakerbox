package com.yammer.breakerbox.service.views;

import com.google.common.collect.ImmutableList;
import com.yammer.breakerbox.service.core.SyncPropertyKeyState;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.tenacity.core.config.TenacityConfiguration;

public class ConfigureView extends NavbarView {
    private final ServiceId serviceId;
    private final Iterable<SyncPropertyKeyState> syncPropertyKeyStates;
    private final TenacityConfiguration tenacityConfiguration;
    private final Iterable<OptionItem> configurationVersions;

    public ConfigureView(ServiceId serviceId,
                         Iterable<SyncPropertyKeyState> syncPropertyKeyStates,
                         TenacityConfiguration tenacityConfiguration,
                         ImmutableList<OptionItem> dependencyEntities) {
        super("/templates/configure/configure.mustache");
        this.serviceId = serviceId;
        this.syncPropertyKeyStates = syncPropertyKeyStates;
        this.tenacityConfiguration = tenacityConfiguration;
        this.configurationVersions = dependencyEntities;
    }

    public ServiceId getServiceId() {
        return serviceId;
    }

    public Iterable<SyncPropertyKeyState> getSyncPropertyKeyStates() {
        return syncPropertyKeyStates;
    }

    public TenacityConfiguration getTenacityConfiguration() {
        return tenacityConfiguration;
    }

    public Iterable<OptionItem> getConfigurationVersions() {
        return configurationVersions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfigureView that = (ConfigureView) o;

        if (!configurationVersions.equals(that.configurationVersions)) return false;
        if (!serviceId.equals(that.serviceId)) return false;
        if (!syncPropertyKeyStates.equals(that.syncPropertyKeyStates)) return false;
        if (!tenacityConfiguration.equals(that.tenacityConfiguration)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = serviceId.hashCode();
        result = 31 * result + syncPropertyKeyStates.hashCode();
        result = 31 * result + tenacityConfiguration.hashCode();
        result = 31 * result + configurationVersions.hashCode();
        return result;
    }
}
