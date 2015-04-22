package com.yammer.breakerbox.service.tenacity;

import com.yammer.breakerbox.service.config.BreakerboxServiceConfiguration;
import com.yammer.tenacity.core.bundle.TenacityBundleBuilder;

public class DelayedTenacityBundleBuilder extends TenacityBundleBuilder<BreakerboxServiceConfiguration> {
    public static TenacityBundleBuilder<BreakerboxServiceConfiguration> newBuilder() {
        return new DelayedTenacityBundleBuilder();
    }

    @Override
    public DelayedTenacityConfiguredBundle build() {
        if (configurationFactory == null) {
            throw new IllegalArgumentException("Must supply a Configuration Factory");
        }

        return new DelayedTenacityConfiguredBundle(
                configurationFactory,
                executionHook,
                exceptionMapperBuilder.build(),
                usingTenacityCircuitBreakerHealthCheck);
    }
}