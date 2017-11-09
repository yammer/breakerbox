package com.yammer.breakerbox.service.tenacity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.netflix.turbine.discovery.Instance;
import com.yammer.breakerbox.turbine.client.TurbineTenacityClient;
import com.yammer.tenacity.core.TenacityCommand;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class TenacityConfigurationFetcher extends TenacityCommand<Optional<TenacityConfiguration>> {
    public static class Factory {
        private final TurbineTenacityClient client;

        public Factory(TurbineTenacityClient client) {
            this.client = client;
        }

        public TenacityConfigurationFetcher create(Instance instance, TenacityPropertyKey key) {
            return new TenacityConfigurationFetcher(client, instance, key);
        }
    }

    private static class Key {
        private final Instance instance;
        private final TenacityPropertyKey tenacityPropertyKey;

        public Key(Instance instance, TenacityPropertyKey tenacityPropertyKey) {
            this.instance = checkNotNull(instance);
            this.tenacityPropertyKey = checkNotNull(tenacityPropertyKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(instance, tenacityPropertyKey);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final Key other = (Key) obj;
            return Objects.equals(this.instance, other.instance)
                    && Objects.equals(this.tenacityPropertyKey, other.tenacityPropertyKey);
        }
    }

    private final TurbineTenacityClient client;
    private final Key key;
    private static final Cache<Key, TenacityConfiguration> cache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS) //Small TTL to collapse simultaneous requests
            .build();
    private static final Logger LOGGER = LoggerFactory.getLogger(TenacityConfigurationFetcher.class);

    public TenacityConfigurationFetcher(TurbineTenacityClient client,
                                        Instance instance,
                                        TenacityPropertyKey key) {
        super(BreakerboxDependencyKey.BRKRBX_SERVICES_CONFIGURATION);
        this.client = checkNotNull(client);
        this.key = new Key(checkNotNull(instance), checkNotNull(key));
    }

    @Override
    protected Optional<TenacityConfiguration> run() throws Exception {
        try {
            return Optional.of(cache.get(key, () ->
                client.getTenacityConfiguration(key.instance, key.tenacityPropertyKey).orElse(null)));
        } catch (CacheLoader.InvalidCacheLoadException err) {
            //null was returned, don't negatively cache results
        } catch (Exception err) {
            LOGGER.warn("Unexpected exception", err);
        }
        return Optional.empty();
    }

    @Override
    protected Optional<TenacityConfiguration> getFallback() {
        return Optional.empty();
    }
}