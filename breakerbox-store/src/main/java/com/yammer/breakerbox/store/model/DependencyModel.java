package com.yammer.breakerbox.store.model;

import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import org.joda.time.DateTime;

import java.util.Objects;

public class DependencyModel {
    private final DependencyId dependencyId;
    private final DateTime dateTime;
    private final TenacityConfiguration tenacityConfiguration;
    private final String user;
    private final ServiceId serviceId;

    public DependencyModel(DependencyId dependencyId,
                           DateTime dateTime,
                           TenacityConfiguration tenacityConfiguration,
                           String user,
                           ServiceId serviceId) {
        this.dependencyId = dependencyId;
        this.dateTime = dateTime;
        this.tenacityConfiguration = tenacityConfiguration;
        this.user = user;
        this.serviceId = serviceId;
    }

    public DependencyId getDependencyId() {
        return dependencyId;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public TenacityConfiguration getTenacityConfiguration() {
        return tenacityConfiguration;
    }

    public String getUser() {
        return user;
    }

    public ServiceId getServiceId() {
        return serviceId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencyId, dateTime, tenacityConfiguration, user, serviceId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final DependencyModel other = (DependencyModel) obj;
        return Objects.equals(this.dependencyId, other.dependencyId)
                && Objects.equals(this.dateTime, other.dateTime)
                && Objects.equals(this.tenacityConfiguration, other.tenacityConfiguration)
                && Objects.equals(this.user, other.user)
                && Objects.equals(this.serviceId, other.serviceId);
    }
}
