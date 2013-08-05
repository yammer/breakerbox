package com.yammer.homie.service.azure;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;
import com.yammer.homie.service.auth.User;
import com.yammer.homie.service.ldap.VirtualGroup;

import static com.google.common.base.Preconditions.checkNotNull;

public class AccessEntityKey implements TableKey {
    private final User partitionKey;
    private final VirtualGroup rowKey;
    private final Class<AccessEntity> klass = AccessEntity.class;

    public AccessEntityKey(User user, VirtualGroup group) {
        this.partitionKey = checkNotNull(user, "user cannot be null");
        this.rowKey = checkNotNull(group, "group cannot be null");
    }

    @Override
    public String getRowKey() {
        return rowKey.getName();
    }

    @Override
    public String getPartitionKey() {
        return partitionKey.getUsername();
    }

    @Override
    public Class<? extends TableServiceEntity> getEntityClass() {
        return klass;
    }

    @Override
    public AzureTableName getTable() {
        return AzureTableName.ACCESS;
    }
}
