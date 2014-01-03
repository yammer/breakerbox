package com.yammer.breakerbox.azure.core;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

public interface TableKey {
    String getRowKey();
    String getPartitionKey();
    Class<? extends TableServiceEntity> getEntityClass();
    AzureTableName getTable();
}