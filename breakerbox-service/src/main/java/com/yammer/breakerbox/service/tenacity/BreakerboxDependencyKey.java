package com.yammer.breakerbox.service.tenacity;

import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;

public enum BreakerboxDependencyKey implements TenacityPropertyKey, TenacityPropertyKeyFactory {
    BRKRBX_SERVICES_PROPERTYKEYS, BRKRBX_SERVICES_CONFIGURATION;

    @Override
    public TenacityPropertyKey from(String value) {
        return valueOf(value);
    }
}