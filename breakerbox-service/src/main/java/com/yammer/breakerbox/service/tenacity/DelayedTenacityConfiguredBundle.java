package com.yammer.breakerbox.service.tenacity;

import com.google.common.base.Optional;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.yammer.breakerbox.service.config.BreakerboxServiceConfiguration;
import com.yammer.tenacity.core.bundle.TenacityBundleConfigurationFactory;
import com.yammer.tenacity.core.bundle.TenacityConfiguredBundle;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import javax.ws.rs.ext.ExceptionMapper;
import java.util.Map;

public class DelayedTenacityConfiguredBundle extends TenacityConfiguredBundle<BreakerboxServiceConfiguration> {
    public DelayedTenacityConfiguredBundle(TenacityBundleConfigurationFactory<BreakerboxServiceConfiguration> tenacityBundleConfigurationFactory,
                                           Optional<HystrixCommandExecutionHook> hystrixCommandExecutionHook,
                                           Iterable<ExceptionMapper<? extends Throwable>> exceptionMappers,
                                           boolean usingTenacityCircuitBreakerHealthCheck) {
        super(tenacityBundleConfigurationFactory, hystrixCommandExecutionHook, exceptionMappers, usingTenacityCircuitBreakerHealthCheck);
    }

    public void delayedRegisterTenacityProperties(
            Map<TenacityPropertyKey, TenacityConfiguration> tenacityPropertyKeyConfigurations,
            BreakerboxServiceConfiguration configuration) {
        super.registerTenacityProperties(tenacityPropertyKeyConfigurations, configuration);
    }
}