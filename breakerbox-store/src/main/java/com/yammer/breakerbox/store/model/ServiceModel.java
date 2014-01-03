package com.yammer.breakerbox.store.model;

import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceModel that = (ServiceModel) o;

        if (!dependencyId.equals(that.dependencyId)) return false;
        if (!serviceId.equals(that.serviceId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = serviceId.hashCode();
        result = 31 * result + dependencyId.hashCode();
        return result;
    }
}
