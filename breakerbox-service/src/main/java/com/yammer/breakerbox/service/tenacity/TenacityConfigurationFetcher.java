package com.yammer.breakerbox.service.tenacity;

import com.google.common.base.Optional;
import com.yammer.tenacity.client.TenacityClient;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import java.net.URI;

public class TenacityConfigurationFetcher extends TenacityCommand<Optional<TenacityConfiguration>> {
    public static class Factory {
        private final TenacityClient client;

        public Factory(TenacityClient client) {
            this.client = client;
        }

        public TenacityConfigurationFetcher create(URI root, TenacityPropertyKey key) {
            return new TenacityConfigurationFetcher(client, root, key);
        }
    }

    private final TenacityClient client;
    private final URI uri;
    private final TenacityPropertyKey key;

    public TenacityConfigurationFetcher(TenacityClient client,
                                        URI uri,
                                        TenacityPropertyKey key) {
        super(BreakerboxDependencyKey.BRKRBX_SERVICES_CONFIGURATION);
        this.client = client;
        this.uri = uri;
        this.key = key;
    }

    @Override
    protected Optional<TenacityConfiguration> run() throws Exception {
        return client.getTenacityConfiguration(uri, key);
    }

    @Override
    protected Optional<TenacityConfiguration> getFallback() {
        return Optional.absent();
    }
}