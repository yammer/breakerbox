package com.yammer.breakerbox.service.views;

import com.yammer.breakerbox.service.core.ServiceId;
import com.yammer.dropwizard.views.View;
import com.yammer.tenacity.core.config.TenacityConfiguration;

public class ConfigureView extends View {
    private final ServiceId serviceId;

    public ConfigureView(ServiceId serviceId) {
        super("/templates/configure/configure.mustache");
        this.serviceId = serviceId;
    }

    public ServiceId getServiceId() {
        return serviceId;
    }

    public TenacityConfiguration getTenacityConfiguration() {
        return new TenacityConfiguration();
    }
}
