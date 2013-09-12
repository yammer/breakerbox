package com.yammer.breakerbox.service.views;

import com.yammer.breakerbox.service.core.ServiceId;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigureView extends NavbarView {
    private final ServiceId serviceId;
    private final Iterable<String> serviceDependencies;
    private final TenacityConfiguration tenacityConfiguration;
    private final Iterable<Pair<String,String>> configurationVersions;

    public ConfigureView(ServiceId serviceId,
                         Iterable<String> serviceDependencies,
                         TenacityConfiguration tenacityConfiguration,
                         Iterable<Pair<String, String>> dependencyEntities) {
        super("/templates/configure/configure.mustache");
        this.serviceId = serviceId;
        this.serviceDependencies = serviceDependencies;
        this.tenacityConfiguration = tenacityConfiguration;
        this.configurationVersions = dependencyEntities;
    }

    public ServiceId getServiceId() {
        return serviceId;
    }

    public Iterable<String> getServiceDependencies() {
        return serviceDependencies;
    }

    public TenacityConfiguration getTenacityConfiguration() {
        return tenacityConfiguration;
    }

    public Iterable<Pair<String, String>> getConfigurationVersions() {
        return configurationVersions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfigureView that = (ConfigureView) o;

        if (!serviceDependencies.equals(that.serviceDependencies)) return false;
        if (!serviceId.equals(that.serviceId)) return false;
        if (!tenacityConfiguration.equals(that.tenacityConfiguration)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = serviceId.hashCode();
        result = 31 * result + serviceDependencies.hashCode();
        result = 31 * result + tenacityConfiguration.hashCode();
        return result;
    }
}
