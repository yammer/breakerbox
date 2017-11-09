package com.yammer.breakerbox.store.model;

import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;

import java.util.Objects;

public class ServiceModel {
    private final ServiceId serviceId;
    private final DependencyId dependencyId;

    public ServiceModel(ServiceId serviceId, DependencyId dependencyId) {
        this.serviceId = serviceId;
        this.dependencyId = dependencyId;
    }

    public ServiceId getServiceId() {
        return serviceId;
    }

    public DependencyId getDependencyId() {
        return dependencyId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, dependencyId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ServiceModel other = (ServiceModel) obj;
        return Objects.equals(this.serviceId, other.serviceId)
                && Objects.equals(this.dependencyId, other.dependencyId);
    }
}
