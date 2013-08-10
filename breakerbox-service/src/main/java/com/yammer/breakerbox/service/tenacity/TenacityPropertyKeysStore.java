package com.yammer.breakerbox.service.tenacity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TenacityPropertyKeysStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(TenacityPropertyKeysStore.class);
    private final Cache<URI, ImmutableList<String>> tenacityPropertyKeyCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();
    private final TenacityPoller.Factory tenacityPollerFactory;

    public TenacityPropertyKeysStore(TenacityPoller.Factory tenacityPollerFactory) {
        this.tenacityPollerFactory = tenacityPollerFactory;
    }

    public ImmutableList<String> getTenacityPropertyKeys(URI uri) {
        final TenacityPoller tenacityPoller = tenacityPollerFactory.create(uri);
        try {
            return tenacityPropertyKeyCache.get(uri, new Callable<ImmutableList<String>>() {
                @Override
                public ImmutableList<String> call() throws Exception {
                    return tenacityPoller.execute().get();
                }
            });
        } catch (ExecutionException err) {
            LOGGER.warn("Unexpected exception", err);
        }
        return tenacityPoller.getFallback().or(ImmutableList.<String>of());
    }

    public ImmutableMap<URI, ImmutableList<String>> snapshotMap() {
        return ImmutableMap.copyOf(tenacityPropertyKeyCache.asMap());
    }
}