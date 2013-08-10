package com.yammer.breakerbox.service.tenacity;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.yammer.tenacity.client.TenacityClient;
import com.yammer.tenacity.core.TenacityCommand;

import java.net.URI;

public class TenacityPoller extends TenacityCommand<Optional<ImmutableList<String>>> {
    public static class Factory {
        private final TenacityClient tenacityClient;

        public Factory(TenacityClient tenacityClient) {
            this.tenacityClient = tenacityClient;
        }

        public TenacityPoller create(URI uri) {
            return new TenacityPoller(tenacityClient, uri);
        }
    }

    private final TenacityClient tenacityClient;
    private final URI uri;

    public TenacityPoller(TenacityClient tenacityClient,
                          URI uri) {
        super(BreakerboxDependencyKey.BRKRBX_SERVICES);
        this.tenacityClient = tenacityClient;
        this.uri = uri;
    }

    @Override
    protected Optional<ImmutableList<String>> run() throws Exception {
        return tenacityClient.getTenacityPropertyKeys(uri);
    }

    @Override
    protected Optional<ImmutableList<String>> getFallback() {
        return Optional.absent();
    }
}