package com.yammer.breakerbox.jdbi;

import com.google.common.base.Optional;
import com.yammer.breakerbox.store.BreakerboxStore;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.store.model.DependencyModel;
import com.yammer.breakerbox.store.model.ServiceModel;
import com.yammer.dropwizard.config.Environment;
import org.joda.time.DateTime;

public class JdbiStore extends BreakerboxStore {
    public JdbiStore(JdbiConfiguration storeConfiguration, Environment environment) {
        super(storeConfiguration, environment);
    }

    @Override
    public boolean initialize() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean store(DependencyModel dependencyModel) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean store(ServiceModel serviceModel) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean delete(ServiceModel serviceModel) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean delete(DependencyModel dependencyModel) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean delete(ServiceId serviceId, DependencyId dependencyId) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean delete(DependencyId dependencyId, DateTime dateTime, ServiceId serviceId) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Optional<ServiceModel> retrieve(ServiceId serviceId, DependencyId dependencyId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Optional<DependencyModel> retrieve(DependencyId dependencyId, DateTime dateTime, ServiceId serviceId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Optional<DependencyModel> retrieveLatest(DependencyId dependencyId, ServiceId serviceId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterable<ServiceModel> allServiceModels() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterable<ServiceModel> listDependenciesFor(ServiceId serviceId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterable<DependencyModel> allDependenciesFor(DependencyId dependencyId, ServiceId serviceId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
