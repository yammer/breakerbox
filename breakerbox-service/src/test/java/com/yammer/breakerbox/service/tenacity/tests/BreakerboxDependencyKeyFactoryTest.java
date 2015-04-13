package com.yammer.breakerbox.service.tenacity.tests;

import com.yammer.breakerbox.service.tenacity.BreakerboxDependencyKey;
import com.yammer.breakerbox.service.tenacity.BreakerboxDependencyKeyFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BreakerboxDependencyKeyFactoryTest {
    private final BreakerboxDependencyKeyFactory keyFactory = new BreakerboxDependencyKeyFactory();

    @Test
    public void from() {
        assertThat(keyFactory.from("brkrbx_services_propertykeys".toUpperCase()))
                .isEqualTo(BreakerboxDependencyKey.BRKRBX_SERVICES_PROPERTYKEYS);
    }
}