package com.yammer.breakerbox.service.azure;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;
import com.yammer.azure.core.AzureTableName;
import com.yammer.azure.core.TableKey;
import com.yammer.azure.core.TableType;
import com.yammer.breakerbox.service.core.ServiceId;

public class ServiceEntity extends TableType implements TableKey {
    @Deprecated
    public ServiceEntity() {
        super(TableId.SERVICES);
    }

    public ServiceEntity(ServiceId serviceId) {
        super(TableId.SERVICES);
        this.partitionKey = TableId.SERVICES.toString();
        this.rowKey = serviceId.toString();
    }

    public ServiceId getServiceId() {
        return ServiceId.from(getRowKey());
    }

    @Override
    public Class<? extends TableServiceEntity> getEntityClass() {
        return ServiceEntity.class;
    }

    @Override
    public AzureTableName getTable() {
        return TableId.SERVICES;
    }
}