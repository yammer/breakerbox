package com.yammer.breakerbox.azure.core;

import com.microsoft.azure.storage.table.TableServiceEntity;

public interface TableKey {
    String getRowKey();
    String getPartitionKey();
    Class<? extends TableServiceEntity> getEntityClass();
    AzureTableName getTable();
}