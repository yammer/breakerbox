package com.yammer.breakerbox.jdbi;

import com.yammer.breakerbox.store.model.ServiceModel;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

public interface ServiceDB {
    @SqlQuery("select * from service where name = :service.serviceId and dependency = :service.dependencyId")
    @Mapper(Mappers.ServiceModelMapper.class)
    public ServiceModel find(@BindBean("service") ServiceModel service);

    @SqlUpdate("insert into service (name, dependency) values (:service.serviceId, :service.dependencyId)")
    public void insert(@BindBean("service") ServiceModel service);
}