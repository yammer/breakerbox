package com.yammer.homie.service.azure;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class RequestEntityKey implements TableKey {
    private final UUID rowKey;
    private final Class<RequestEntity> klass = RequestEntity.class;

    public RequestEntityKey(UUID rowKey) {
        this.rowKey = checkNotNull(rowKey, "rowKey cannot be null");
    }

    @Override
    public String getRowKey() {
        return rowKey.toString();
    }

    @Override
    public String getPartitionKey() {
        return AzureTableName.REQUEST.toString();
    }

    @Override
    public Class<? extends TableServiceEntity> getEntityClass() {
        return klass;
    }

    @Override
    public AzureTableName getTable() {
        return AzureTableName.REQUEST;
    }
}