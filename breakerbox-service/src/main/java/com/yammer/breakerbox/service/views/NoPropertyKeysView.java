package com.yammer.breakerbox.service.views;

import com.yammer.breakerbox.service.core.ServiceId;

public class NoPropertyKeysView extends NavbarView {
    private final ServiceId serviceId;

    public NoPropertyKeysView(ServiceId serviceId) {
        super("/templates/errors/nopropertykeys.mustache");
        this.serviceId = serviceId;
    }

    public ServiceId getServiceId() {
        return serviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NoPropertyKeysView that = (NoPropertyKeysView) o;

        if (!serviceId.equals(that.serviceId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return serviceId.hashCode();
    }
}
