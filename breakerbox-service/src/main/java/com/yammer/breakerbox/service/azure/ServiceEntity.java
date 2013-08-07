package com.yammer.breakerbox.service.azure;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;
import com.yammer.azure.core.AzureTableName;
import com.yammer.azure.core.TableKey;
import com.yammer.azure.core.TableType;
import com.yammer.breakerbox.service.core.DependencyId;
import com.yammer.breakerbox.service.core.ServiceId;

public class ServiceEntity extends TableType implements TableKey {
    public ServiceEntity(ServiceId serviceId, DependencyId dependencyId) {
        super(TableId.SERVICES);
        this.partitionKey = serviceId.getId();
        this.rowKey = dependencyId.toString();
    }

    public ServiceId getServiceId() {
        return ServiceId.from(getPartitionKey());
    }

    public DependencyId getDependencyId() {
        return DependencyId.from(getRowKey());
    }

    @Override
    public Class<? extends TableServiceEntity> getEntityClass() {
        return ServiceEntity.class;
    }

    @Override
    public AzureTableName getTable() {
        return TableId.SERVICES;
    }

    @Override
    public String toString() {
        return getServiceId().toString();
    }

    /** For Azure */

    @Deprecated
    public ServiceEntity() {
        super(TableId.SERVICES);
    }

}