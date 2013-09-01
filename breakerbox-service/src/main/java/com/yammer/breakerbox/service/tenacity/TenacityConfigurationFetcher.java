package com.yammer.breakerbox.service.tenacity;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.yammer.tenacity.client.TenacityClient;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

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

    private static class Key {
        private final URI uri;
        private final TenacityPropertyKey tenacityPropertyKey;

        public Key(URI uri, TenacityPropertyKey tenacityPropertyKey) {
            this.uri = checkNotNull(uri);
            this.tenacityPropertyKey = checkNotNull(tenacityPropertyKey);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (!tenacityPropertyKey.equals(key.tenacityPropertyKey)) return false;
            if (!uri.equals(key.uri)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = uri.hashCode();
            result = 31 * result + tenacityPropertyKey.hashCode();
            return result;
        }
    }

    private final TenacityClient client;
    private final Key key;
    private static final Cache<Key, TenacityConfiguration> cache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS) //Small TTL to collapse simultaneous requests
            .build();
    private static final Logger LOGGER = LoggerFactory.getLogger(TenacityConfigurationFetcher.class);

    public TenacityConfigurationFetcher(TenacityClient client,
                                        URI uri,
                                        TenacityPropertyKey key) {
        super(BreakerboxDependencyKey.BRKRBX_SERVICES_CONFIGURATION);
        this.client = checkNotNull(client);
        this.key = new Key(checkNotNull(uri), checkNotNull(key));
    }

    @Override
    protected Optional<TenacityConfiguration> run() throws Exception {
        try {
            return Optional.of(cache.get(key, new Callable<TenacityConfiguration>() {
                @Override
                public TenacityConfiguration call() throws Exception {
                    return client.getTenacityConfiguration(key.uri, key.tenacityPropertyKey).orNull();
                }
            }));
        } catch (CacheLoader.InvalidCacheLoadException err) {
            //null was returned, don't negatively cache results
        } catch (Exception err) {
            LOGGER.warn("Unexpected exception", err);
        }
        return Optional.absent();
    }

    @Override
    protected Optional<TenacityConfiguration> getFallback() {
        return Optional.absent();
    }
}