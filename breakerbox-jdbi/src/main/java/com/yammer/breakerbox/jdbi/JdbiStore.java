package com.yammer.breakerbox.jdbi;

import com.google.common.base.Optional;
import com.yammer.breakerbox.jdbi.args.DateTimeArgumentFactory;
import com.yammer.breakerbox.jdbi.args.DependencyIdArgumentFactory;
import com.yammer.breakerbox.jdbi.args.ServiceIdArgumentFactory;
import com.yammer.breakerbox.jdbi.args.TenacityConfigurationArgumentFactory;
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
    private final DependencyDB dependencyDB;

    public JdbiStore(JdbiConfiguration storeConfiguration, Environment environment) throws Exception {
        this(storeConfiguration, environment, new DBIFactory().build(environment, storeConfiguration, "breakerbox"));
    }

    public JdbiStore(JdbiConfiguration storeConfiguration, Environment environment, DBI database) {
        super(storeConfiguration, environment);
        this.database = database;
        this.database.registerArgumentFactory(new DependencyIdArgumentFactory());
        this.database.registerArgumentFactory(new ServiceIdArgumentFactory());
        this.database.registerArgumentFactory(new TenacityConfigurationArgumentFactory());
        this.database.registerArgumentFactory(new DateTimeArgumentFactory());

        dependencyDB = database.onDemand(DependencyDB.class);
        serviceDB = database.onDemand(ServiceDB.class);
    }

    @Override
    public boolean initialize() throws Exception {
        return true;
    }

    @Override
    public boolean store(DependencyModel dependencyModel) {
        try {
            return dependencyDB.insert(dependencyModel) == 1;
        } catch (DBIException err) {
            LOGGER.warn("Failed to store: {}", dependencyModel, err);
            return false;
        }
    }

    @Override
    public boolean store(ServiceModel serviceModel) {
        try {
            return serviceDB.insert(serviceModel) == 1;
        } catch (DBIException err) {
            LOGGER.warn("Failed to store: {}", serviceModel, err);
            return false;
        }
    }

    @Override
    public boolean delete(ServiceModel serviceModel) {
        try {
            return serviceDB.delete(serviceModel) >= 0;
        } catch (DBIException err) {
            LOGGER.warn("Failed to delete: {}", serviceModel, err);
            return false;
        }
    }

    @Override
    public boolean delete(DependencyModel dependencyModel) {
        try {
            return dependencyDB.delete(dependencyModel.getDependencyId(), dependencyModel.getDateTime()) >= 0;
        } catch (DBIException err) {
            LOGGER.warn("Failed to delete: {}", dependencyModel, err);
            return false;
        }
    }

    @Override
    public boolean delete(ServiceId serviceId, DependencyId dependencyId) {
        return delete(new ServiceModel(serviceId, dependencyId));
    }

    @Override
    public boolean delete(DependencyId dependencyId, DateTime dateTime) {
        try {
            return dependencyDB.delete(dependencyId, dateTime) >= 0;
        } catch (DBIException err) {
            LOGGER.warn("Failed to delete: {}, {}", dependencyId, dateTime, err);
            return false;
        }
    }

    @Override
    public Optional<ServiceModel> retrieve(ServiceId serviceId, DependencyId dependencyId) {
        try {
            return Optional.fromNullable(serviceDB.find(new ServiceModel(serviceId, dependencyId)));
        } catch (DBIException err) {
            LOGGER.warn("Failed to retrieve {}:{}", serviceId, dependencyId, err);
            return Optional.absent();
        }
    }

    @Override
    public Optional<DependencyModel> retrieve(DependencyId dependencyId, DateTime dateTime) {
        try {
            return Optional.fromNullable(dependencyDB.find(dependencyId, dateTime));
        } catch (DBIException err) {
            LOGGER.warn("Failed to retrieve {}, {}", dependencyId, dateTime.getMillis(), err);
            return Optional.absent();
        }
    }

    @Override
    public Optional<DependencyModel> retrieveLatest(DependencyId dependencyId, ServiceId serviceId) {
        try {
            return Optional.fromNullable(dependencyDB.findLatest(dependencyId, serviceId));
        } catch (DBIException err) {
            LOGGER.warn("Failed to retrieve {}, {}", dependencyId, serviceId, err);
            return Optional.absent();
        }
    }

    @Override
    public Iterable<ServiceModel> allServiceModels() {
        return serviceDB.all();
    }

    @Override
    public Iterable<ServiceModel> listDependenciesFor(ServiceId serviceId) {
        return serviceDB.all(serviceId);
    }

    @Override
    public Iterable<DependencyModel> allDependenciesFor(DependencyId dependencyId, ServiceId serviceId) {
        return dependencyDB.all(dependencyId, serviceId);
    }
}