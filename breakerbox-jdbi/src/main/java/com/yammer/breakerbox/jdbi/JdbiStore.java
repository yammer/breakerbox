package com.yammer.breakerbox.jdbi;

import com.google.common.base.Optional;
import com.yammer.breakerbox.jdbi.args.DependencyIdArgumentFactory;
import com.yammer.breakerbox.jdbi.args.ServiceIdArgumentFactory;
import com.yammer.breakerbox.store.BreakerboxStore;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.store.model.DependencyModel;
import com.yammer.breakerbox.store.model.ServiceModel;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jdbi.DBIFactory;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.exceptions.DBIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbiStore extends BreakerboxStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbiStore.class);
    private final DBI database;
    private final ServiceDB serviceDB;

    public JdbiStore(JdbiConfiguration storeConfiguration, Environment environment) throws Exception {
        this(storeConfiguration, environment, new DBIFactory().build(environment, storeConfiguration, "breakerbox"));
    }

    public JdbiStore(JdbiConfiguration storeConfiguration, Environment environment, DBI database) {
        super(storeConfiguration, environment);
        this.database = database;
        this.database.registerArgumentFactory(new DependencyIdArgumentFactory());
        this.database.registerArgumentFactory(new ServiceIdArgumentFactory());

        serviceDB = database.onDemand(ServiceDB.class);
    }

    @Override
    public boolean initialize() throws Exception {
        return true;
    }

    @Override
    public boolean store(DependencyModel dependencyModel) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean store(ServiceModel serviceModel) {
        serviceDB.insert(serviceModel);
        return true;
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
        try {
            return Optional.of(serviceDB.find(new ServiceModel(serviceId, dependencyId)));
        } catch (DBIException err) {
            LOGGER.warn("Failed to retrieve {}:{}", serviceId, dependencyId, err);
            return Optional.absent();
        }
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
