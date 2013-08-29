package com.yammer.breakerbox.service.tenacity;

import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyKeyFactory;

public class BreakerboxDependencyKeyFactory implements TenacityPropertyKeyFactory {
    @Override
    public TenacityPropertyKey from(String value) {
        return BreakerboxDependencyKey.valueOf(value.toUpperCase());
    }
}