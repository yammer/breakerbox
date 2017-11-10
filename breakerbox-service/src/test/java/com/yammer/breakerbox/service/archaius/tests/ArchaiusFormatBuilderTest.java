package com.yammer.breakerbox.service.archaius.tests;

import com.yammer.breakerbox.service.archaius.ArchaiusFormatBuilder;
import com.yammer.breakerbox.service.tenacity.BreakerboxDependencyKey;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArchaiusFormatBuilderTest {
    @Test
    public void empty() {
        assertThat(ArchaiusFormatBuilder.builder().build())
                .isEmpty();
    }

    @Test
    public void single() {
        assertThat(ArchaiusFormatBuilder
                .builder()
                .with(BreakerboxDependencyKey.BRKRBX_SERVICES_PROPERTYKEYS, new TenacityConfiguration())
                .build())
                .contains("hystrix.command.BRKRBX_SERVICES_PROPERTYKEYS.execution.isolation.thread.timeoutInMilliseconds=1000")
                .contains("hystrix.command.BRKRBX_SERVICES_PROPERTYKEYS.metrics.rollingStats.numBuckets=10");
    }

    private enum DependencyKeys implements TenacityPropertyKey {
        ONE, TWO
    }

    @Test
    public void multiple() {
        assertThat(ArchaiusFormatBuilder
                .builder()
                .with(DependencyKeys.ONE, new TenacityConfiguration())
                .with(DependencyKeys.TWO, new TenacityConfiguration())
                .build())
                .contains("hystrix.command.ONE.execution.isolation.thread.timeoutInMilliseconds=1000")
                .contains("hystrix.command.ONE.metrics.rollingStats.numBuckets=10")
                .contains("hystrix.command.TWO.execution.isolation.thread.timeoutInMilliseconds=1000")
                .contains("hystrix.command.TWO.metrics.rollingStats.numBuckets=10");
    }
}
