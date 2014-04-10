package com.yammer.breakerbox.store;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.yammer.breakerbox.store.model.DependencyModel;
import com.yammer.breakerbox.store.model.ServiceModel;
import com.yammer.dropwizard.config.Environment;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import org.joda.time.DateTime;

import java.util.concurrent.TimeUnit;


public abstract class BreakerboxStore {
    protected final Cache<ServiceId, ImmutableList<ServiceModel>> listDependenciesCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    protected static final Timer LIST_SERVICES = Metrics.newTimer(BreakerboxStore.class, "list-services");
    protected static final Timer LIST_SERVICE = Metrics.newTimer(BreakerboxStore.class, "list-service");
    protected static final Timer DEPENDENCY_CONFIGS = Metrics.newTimer(BreakerboxStore.class, "latest-dependency-config");

    @SuppressWarnings("unused")
    protected <StoreConfiguration> BreakerboxStore(StoreConfiguration storeConfiguration, Environment environment) {}

    public abstract boolean initialize();
    public abstract boolean store(DependencyModel dependencyModel);
    public abstract boolean store(ServiceModel serviceModel);
    public abstract boolean delete(ServiceModel serviceModel);
    public abstract boolean delete(DependencyModel dependencyModel);
    public abstract boolean delete(ServiceId serviceId, DependencyId dependencyId);
    public abstract boolean delete(DependencyId dependencyId, DateTime dateTime, ServiceId serviceId);
    public abstract Optional<ServiceModel> retrieve(ServiceId serviceId, DependencyId dependencyId);
    public abstract Optional<DependencyModel> retrieve(DependencyId dependencyId, DateTime dateTime, ServiceId serviceId);
    public abstract Optional<DependencyModel> retrieveLatest(DependencyId dependencyId, ServiceId serviceId);
    public abstract Iterable<ServiceModel> allServiceModels();
    public abstract Iterable<ServiceModel> listDependenciesFor(ServiceId serviceId);
    public abstract Iterable<DependencyModel> allDependenciesFor(DependencyId dependencyId, ServiceId serviceId);
}