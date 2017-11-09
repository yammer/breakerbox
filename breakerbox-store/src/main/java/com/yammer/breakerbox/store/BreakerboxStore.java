package com.yammer.breakerbox.store;

import com.codahale.metrics.Timer;
import com.yammer.breakerbox.store.model.DependencyModel;
import com.yammer.breakerbox.store.model.ServiceModel;
import io.dropwizard.setup.Environment;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;


public abstract class BreakerboxStore {
    protected final Timer listServices;
    protected final Timer listService;
    protected final Timer dependencyConfigs;

    @SuppressWarnings("unused")
    protected <StoreConfiguration> BreakerboxStore(StoreConfiguration storeConfiguration,
                                                   Environment environment) {
        this.listServices = environment.metrics().timer(name(BreakerboxStore.class, "list-services"));
        this.listService = environment.metrics().timer(name(BreakerboxStore.class, "list-service"));
        this.dependencyConfigs = environment.metrics().timer(name(BreakerboxStore.class, "latest-dependency-config"));
    }

    public abstract boolean initialize() throws Exception;
    public abstract boolean store(DependencyModel dependencyModel);
    public abstract boolean store(ServiceModel serviceModel);
    public abstract boolean delete(ServiceModel serviceModel);
    public abstract boolean delete(DependencyModel dependencyModel);
    public abstract boolean delete(ServiceId serviceId, DependencyId dependencyId);
    public abstract boolean delete(DependencyId dependencyId, DateTime dateTime);
    public abstract Optional<ServiceModel> retrieve(ServiceId serviceId, DependencyId dependencyId);
    public abstract Optional<DependencyModel> retrieve(DependencyId dependencyId, DateTime dateTime);
    public abstract Optional<DependencyModel> retrieveLatest(DependencyId dependencyId, ServiceId serviceId);
    public abstract Collection<ServiceModel> allServiceModels();
    public abstract Collection<ServiceModel> listDependenciesFor(ServiceId serviceId);
    public abstract Collection<DependencyModel> allDependenciesFor(DependencyId dependencyId, ServiceId serviceId);
}