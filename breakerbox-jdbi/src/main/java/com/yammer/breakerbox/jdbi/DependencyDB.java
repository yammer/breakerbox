package com.yammer.breakerbox.jdbi;

import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.model.DependencyModel;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(Mappers.DependencyModelMapper.class)
public interface DependencyDB {
    @SqlQuery("select * from dependency where name = :dependency.id and timestamp = :timestamp.millis")
    public DependencyModel find(@BindBean("dependency") DependencyId dependencyId, @BindBean("timestamp") DateTime timestamp);

    @SqlUpdate("insert into dependency (name, timestamp, tenacityConfiguration, user, service) values " +
               "(:dependency.dependencyId, :dependency.dateTime, :dependency.tenacityConfiguration," +
               " :dependency.user, :dependency.serviceId)")
    public int insert(@BindBean("dependency") DependencyModel dependencyModel);

    @SqlUpdate("delete from dependency where name = :dependency.id and timestamp = :timestamp.millis")
    public int delete(@BindBean("dependency") DependencyId dependencyId, @BindBean("timestamp") DateTime timestamp);
}
