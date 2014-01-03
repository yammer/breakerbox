package com.yammer.breakerbox.service.model;

import com.yammer.breakerbox.service.core.DependencyId;
import com.yammer.breakerbox.service.core.ServiceId;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import org.joda.time.DateTime;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DependencyModel that = (DependencyModel) o;

        if (!dateTime.equals(that.dateTime)) return false;
        if (!dependencyId.equals(that.dependencyId)) return false;
        if (!serviceId.equals(that.serviceId)) return false;
        if (!tenacityConfiguration.equals(that.tenacityConfiguration)) return false;
        if (!user.equals(that.user)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dependencyId.hashCode();
        result = 31 * result + dateTime.hashCode();
        result = 31 * result + tenacityConfiguration.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + serviceId.hashCode();
        return result;
    }
}
