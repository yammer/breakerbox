package com.yammer.azure.core;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class TableType extends TableServiceEntity {
    protected final AzureTableName azureTableName;

    protected TableType(AzureTableName azureTableName) {
        this.azureTableName = checkNotNull(azureTableName, "azureTableName cannot be null");
    }

    public AzureTableName getAzureTableName() {
        return azureTableName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TableType)) return false;

        TableType that = (TableType) o;

        if (azureTableName != that.azureTableName) return false;
        if (partitionKey != null ? !partitionKey.equals(that.partitionKey) : that.partitionKey != null) return false;
        if (rowKey != null ? !rowKey.equals(that.rowKey) : that.rowKey != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return azureTableName.hashCode();
    }
}
