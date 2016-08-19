package com.yammer.breakerbox.service.archaius;

import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import com.yammer.tenacity.core.properties.TenacityPropertyRegister;

public class ArchaiusFormatBuilder {
    private final StringBuilder builder = new StringBuilder();

    private ArchaiusFormatBuilder() {}

    public static ArchaiusFormatBuilder builder() {
        return new ArchaiusFormatBuilder();
    }

    public ArchaiusFormatBuilder hystrixMetricsStreamServletMaxConnections(int value) {
        appender("hystrix.stream.maxConcurrentConnections", value);
        appender("hystrix.config.stream.maxConcurrentConnections", value);
        return this;
    }

    public ArchaiusFormatBuilder turbineHostRetryMillis(long value) {
        //This misspelling of retry is on purpose. Unfortunate feature relying on a bug.
        appender("turbine.InstanceMonitor.hostRertyMillis", value);
        return this;
    }

    public ArchaiusFormatBuilder turbineInstanceMonitorLatencyThreshold(long value) {
        appender("turbine.InstanceMonitor.eventStream.skipLineLogic.latencyThreshold", value);
        return this;
    }

    public ArchaiusFormatBuilder turbineInstanceMonitorSkipLineDelay(long value) {
        appender("turbine.InstanceMonitor.eventStream.skipLineLogic.delay", value);
        return this;
    }

    public ArchaiusFormatBuilder with(TenacityPropertyKey key,
                                      TenacityConfiguration configuration) {
        executionIsolationThreadTimeoutInMilliseconds(key, configuration);
        threadpoolCoreSize(key, configuration);
        threadpoolMaxQueueSize(key, configuration);
        threadpoolKeepAliveTimeMinutes(key, configuration);
        threadpoolQueueSizeRejectionThreshold(key, configuration);
        threadpoolMetricsRollingStatsNumBuckets(key, configuration);
        threadpoolMetricsRollingStatsTimeInMilliseconds(key, configuration);
        circuitBreakerRequestVolumeThreshold(key, configuration);
        circuitBreakerErrorThresholdPercentage(key, configuration);
        circuitBreakerSleepWindowInMilliseconds(key, configuration);
        circuitBreakermetricsRollingStatsNumBuckets(key, configuration);
        circuitBreakermetricsRollingStatsTimeInMilliseconds(key, configuration);
        semaphoreMaxConcurrentRequests(key, configuration);
        semaphoreFallbackMaxConcurrentRequests(key, configuration);
        if (configuration.hasExecutionIsolationStrategy()) {
            executionIsolationStrategy(key, configuration);
        }
        return this;
    }

    public String build() {
        return builder.toString();
    }

    private void executionIsolationThreadTimeoutInMilliseconds(TenacityPropertyKey key,
                                                               TenacityConfiguration configuration) {
        appender(TenacityPropertyRegister.executionIsolationThreadTimeoutInMilliseconds(key),
                configuration.getExecutionIsolationThreadTimeoutInMillis());
    }

    private void threadpoolCoreSize(TenacityPropertyKey key, TenacityConfiguration configuration) {
        appender(TenacityPropertyRegister.threadpoolCoreSize(key), configuration.getThreadpool().getThreadPoolCoreSize());
    }

    private void threadpoolMaxQueueSize(TenacityPropertyKey key, TenacityConfiguration configuration) {
        appender(TenacityPropertyRegister.threadpoolMaxQueueSize(key), configuration.getThreadpool().getMaxQueueSize());
    }

    private void threadpoolKeepAliveTimeMinutes(TenacityPropertyKey key, TenacityConfiguration configuration) {
        appender(TenacityPropertyRegister.threadpoolKeepAliveTimeMinutes(key), configuration.getThreadpool().getKeepAliveTimeMinutes());
    }

    private void threadpoolQueueSizeRejectionThreshold(TenacityPropertyKey key, TenacityConfiguration configuration) {
        appender(TenacityPropertyRegister.threadpoolQueueSizeRejectionThreshold(key), configuration.getThreadpool().getQueueSizeRejectionThreshold());
    }

    private void threadpoolMetricsRollingStatsNumBuckets(TenacityPropertyKey key, TenacityConfiguration configuration) {
        appender(TenacityPropertyRegister.threadpoolMetricsRollingStatsNumBuckets(key), configuration.getThreadpool().getMetricsRollingStatisticalWindowBuckets());
    }

    private void threadpoolMetricsRollingStatsTimeInMilliseconds(TenacityPropertyKey key, TenacityConfiguration configuration) {
        appender(TenacityPropertyRegister.threadpoolMetricsRollingStatsTimeInMilliseconds(key), configuration.getThreadpool().getMetricsRollingStatisticalWindowInMilliseconds());
    }

    private void circuitBreakerRequestVolumeThreshold(TenacityPropertyKey key, TenacityConfiguration configuration) {
        appender(TenacityPropertyRegister.circuitBreakerRequestVolumeThreshold(key), configuration.getCircuitBreaker().getRequestVolumeThreshold());
    }

    private void circuitBreakerErrorThresholdPercentage(TenacityPropertyKey key, TenacityConfiguration configuration) {
        appender(TenacityPropertyRegister.circuitBreakerErrorThresholdPercentage(key), configuration.getCircuitBreaker().getErrorThresholdPercentage());
    }

    private void circuitBreakerSleepWindowInMilliseconds(TenacityPropertyKey key, TenacityConfiguration configuration) {
        appender(TenacityPropertyRegister.circuitBreakerSleepWindowInMilliseconds(key), configuration.getCircuitBreaker().getSleepWindowInMillis());
    }

    private void circuitBreakermetricsRollingStatsNumBuckets(TenacityPropertyKey key, TenacityConfiguration configuration) {
        appender(TenacityPropertyRegister.circuitBreakermetricsRollingStatsNumBuckets(key), configuration.getCircuitBreaker().getMetricsRollingStatisticalWindowBuckets());
    }

    private void circuitBreakermetricsRollingStatsTimeInMilliseconds(TenacityPropertyKey key, TenacityConfiguration configuration) {
        appender(TenacityPropertyRegister.circuitBreakermetricsRollingStatsTimeInMilliseconds(key), configuration.getCircuitBreaker().getMetricsRollingStatisticalWindowInMilliseconds());
    }

    private void semaphoreMaxConcurrentRequests(TenacityPropertyKey key, TenacityConfiguration configuration) {
        appender(TenacityPropertyRegister.semaphoreMaxConcurrentRequests(key), configuration.getSemaphore().getMaxConcurrentRequests());
    }

    private void semaphoreFallbackMaxConcurrentRequests(TenacityPropertyKey key, TenacityConfiguration configuration) {
        appender(TenacityPropertyRegister.semaphoreFallbackMaxConcurrentRequests(key), configuration.getSemaphore().getFallbackMaxConcurrentRequests());
    }

    private void executionIsolationStrategy(TenacityPropertyKey key, TenacityConfiguration configuration) {
        appender(TenacityPropertyRegister.executionIsolationStrategy(key), configuration.getExecutionIsolationStrategy());
    }

    private <ValueType> void appender(String key, ValueType value) {
        builder.append(key);
        builder.append('=');
        builder.append(value);
        builder.append('\n');
    }
}
