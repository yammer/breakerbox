package com.yammer.breakerbox.service.tenacity;

import com.yammer.tenacity.core.properties.TenacityPropertyKey;

public enum BreakerboxDependencyKey implements TenacityPropertyKey {
    BRKRBX_SERVICES_PROPERTYKEYS, BRKRBX_SERVICES_CONFIGURATION,
    BRKRBX_LDAP_AUTH
}