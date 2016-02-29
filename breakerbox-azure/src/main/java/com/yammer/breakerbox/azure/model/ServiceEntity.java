package com.yammer.breakerbox.azure.model;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;
import com.yammer.breakerbox.azure.core.AzureTableName;
import com.yammer.breakerbox.azure.core.TableId;
import com.yammer.breakerbox.azure.core.TableKey;
import com.yammer.breakerbox.azure.core.TableType;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean to represent the Service table.
 * Partitioned by service, each service contains a list of service dependency keys.
 * As of 5-SEP-2013, these dependency keys are updated by the continuous polling of services'
 * Tenacity endpoint for DependencyKeys. See TenacityClient for more.
 */
public class ServiceEntity extends TableType implements TableKey {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEntity.class);

    private ServiceEntity(ServiceId serviceId,
                          DependencyId dependencyId) {
        super(TableId.SERVICE);
        this.partitionKey = serviceId.getId();
        this.rowKey = dependencyId.getId();
    }

    public ServiceId getServiceId() {
        return ServiceId.from(getPartitionKey());
    }

    public DependencyId getDependencyId() {
        return DependencyId.from(getRowKey());
    }

    public static ServiceEntity build(ServiceId serviceId,
                                      DependencyId dependencyId) {
        return new ServiceEntity(serviceId, dependencyId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public Class<? extends TableServiceEntity> getEntityClass() {
        return ServiceEntity.class;
    }

    @Override
    public AzureTableName getTable() {
        return TableId.SERVICE;
    }

    /**
     * @deprecated kept for backward compatibility
     * For Azure 
     */

    @Deprecated
    public ServiceEntity() {
        super(TableId.SERVICE);
    }

}
