package com.yammer.breakerbox.service.tenacity;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.netflix.turbine.discovery.Instance;
import com.yammer.breakerbox.service.turbine.client.TurbineTenacityClient;
import com.yammer.tenacity.core.TenacityCommand;

public class TenacityPoller extends TenacityCommand<Optional<ImmutableList<String>>> {
    public static class Factory {
        private final TurbineTenacityClient tenacityClient;

        public Factory(TurbineTenacityClient tenacityClient) {
            this.tenacityClient = tenacityClient;
        }

        public TenacityPoller create(Instance instance) {
            return new TenacityPoller(tenacityClient, instance);
        }
    }

    private final TurbineTenacityClient tenacityClient;
    private final Instance instance;

    public TenacityPoller(TurbineTenacityClient tenacityClient,
                          Instance instance) {
        super(BreakerboxDependencyKey.BRKRBX_SERVICES_PROPERTYKEYS);
        this.tenacityClient = tenacityClient;
        this.instance = instance;
    }

    @Override
    protected Optional<ImmutableList<String>> run() throws Exception {
        return tenacityClient.getTenacityPropertyKeys(instance);
    }

    @Override
    protected Optional<ImmutableList<String>> getFallback() {
        return Optional.absent();
    }
}
