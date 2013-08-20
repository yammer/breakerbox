package com.yammer.breakerbox.service.store;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.yammer.breakerbox.service.tenacity.TenacityPoller;
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
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();
    private final TenacityPoller.Factory tenacityPollerFactory;

    public TenacityPropertyKeysStore(TenacityPoller.Factory tenacityPollerFactory) {
        this.tenacityPollerFactory = tenacityPollerFactory;
    }

    public ImmutableList<String> getTenacityPropertyKeys(final URI uri) {
        try {
            return tenacityPropertyKeyCache.get(uri, new Callable<ImmutableList<String>>() {
                @Override
                public ImmutableList<String> call() throws Exception {
                    return tenacityPollerFactory.create(uri).execute().get();
                }
            });
        } catch (ExecutionException err) {
            LOGGER.warn("Unexpected exception", err);
        }
        return ImmutableList.of();
    }

    public ImmutableMap<URI, ImmutableList<String>> snapshotMap() {
        return ImmutableMap.copyOf(tenacityPropertyKeyCache.asMap());
    }

    public ImmutableSet<String> tenacityPropertyKeysFor(Iterable<URI> uris) {
        return FluentIterable
                .from(uris)
                .transformAndConcat(new Function<URI, ImmutableList<String>>() {
                    @Override
                    public ImmutableList<String> apply(URI input) {
                        return getTenacityPropertyKeys(input);
                    }
                })
                .toSet();
    }
}