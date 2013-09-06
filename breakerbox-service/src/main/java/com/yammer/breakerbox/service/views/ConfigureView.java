package com.yammer.breakerbox.service.views;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.yammer.breakerbox.service.azure.DependencyEntity;
import com.yammer.breakerbox.service.azure.DependencyEntityData;
import com.yammer.breakerbox.service.core.ServiceId;
import com.yammer.tenacity.core.config.TenacityConfiguration;

public class ConfigureView extends NavbarView {
    private final ServiceId serviceId;
    private final Iterable<String> serviceDependencies;
    private final TenacityConfiguration tenacityConfiguration;
    private final Iterable<String> configurationVersions;

    public ConfigureView(ServiceId serviceId,
                         Iterable<String> serviceDependencies,
                         TenacityConfiguration tenacityConfiguration,
                         Iterable<DependencyEntity> dependencyEntities) {
        super("/templates/configure/configure.mustache");
        this.serviceId = serviceId;
        this.serviceDependencies = serviceDependencies;
        this.tenacityConfiguration = tenacityConfiguration;

        //TODO: fix this awful awful thing living in the view
        final ImmutableList.Builder<String> builder = ImmutableList.<String>builder();
        for (DependencyEntity dependencyEntity : dependencyEntities) {
            final Optional<DependencyEntityData> dependencyData = dependencyEntity.getDependencyData();
            if(dependencyData.isPresent()){
                builder.add(dependencyEntity.getRowKey() + " " + dependencyData.get().getUser());
            }
        }
        configurationVersions = builder.build();
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

    public Iterable<String> getConfigurationVersions() {
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
