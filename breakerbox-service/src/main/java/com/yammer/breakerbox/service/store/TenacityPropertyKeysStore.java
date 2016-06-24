package com.yammer.breakerbox.service.store;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;
import com.netflix.turbine.discovery.Instance;
import com.yammer.breakerbox.service.tenacity.TenacityPoller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * TenacityPropertyKeysStore is a component used to track known keys for presentation in the Breakerbox
 * configuration front-end. Adding keys to any externally hosted data structures happens outside the
 * context of this class.
 */
public class TenacityPropertyKeysStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(TenacityPropertyKeysStore.class);
    private final Cache<Instance, ImmutableList<String>> tenacityPropertyKeyCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();
    private final TenacityPoller.Factory tenacityPollerFactory;

    public TenacityPropertyKeysStore(TenacityPoller.Factory tenacityPollerFactory) {
        this.tenacityPollerFactory = tenacityPollerFactory;
    }

    public ImmutableList<String> getTenacityPropertyKeys(Instance instance) {
        try {
            return tenacityPropertyKeyCache.get(instance, () ->
                    tenacityPollerFactory.create(instance).execute().orNull());
        } catch (CacheLoader.InvalidCacheLoadException err) {
            //null was returned
        } catch (Exception err) {
            LOGGER.warn("Unexpected exception", err);
        }
        return ImmutableList.of();
    }

    public Set<String> tenacityPropertyKeysFor(Collection<Instance> instances) {
        return instances
                .stream()
                .map(this::getTenacityPropertyKeys)
                .flatMap(Collection::stream)
                .sorted()
                .collect(Collectors.toSet());
    }
}
