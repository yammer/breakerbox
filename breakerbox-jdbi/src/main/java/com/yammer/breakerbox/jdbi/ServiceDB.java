package com.yammer.breakerbox.jdbi;

import com.google.common.collect.ImmutableList;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.store.model.ServiceModel;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(Mappers.ServiceModelMapper.class)
public interface ServiceDB {
    @SqlQuery("select * from service where name = :service.serviceId and dependency = :service.dependencyId")
    ServiceModel find(@BindBean("service") ServiceModel service);

    @SqlUpdate("insert into service (name, dependency) values (:service.serviceId, :service.dependencyId)")
    int insert(@BindBean("service") ServiceModel service);

    @SqlUpdate("delete from service where name = :service.serviceId and dependency = :service.dependencyId")
    int delete(@BindBean("service") ServiceModel serviceModel);

    @SqlQuery("select * from service")
    ImmutableList<ServiceModel> all();

    @SqlQuery("select * from service where name = :serviceId.id")
    ImmutableList<ServiceModel> all(@BindBean("serviceId") ServiceId serviceId);
}