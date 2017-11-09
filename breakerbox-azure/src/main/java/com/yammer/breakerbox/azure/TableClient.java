package com.yammer.breakerbox.azure;

import com.google.common.collect.ImmutableList;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.*;
import com.yammer.breakerbox.azure.core.AzureTableName;
import com.yammer.breakerbox.azure.core.TableKey;
import com.yammer.breakerbox.azure.core.TableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class TableClient {

    private final CloudTableClient cloudTableClient;
    private static final Logger LOG = LoggerFactory.getLogger(TableClient.class);

    public TableClient(CloudTableClient cloudTableClient) {
        this.cloudTableClient = checkNotNull(cloudTableClient, "cloudTableClient cannot be null");
    }

    private CloudTable tableRefrence(AzureTableName table) {
        try {
            return cloudTableClient.getTableReference(table.toString());
        } catch (URISyntaxException e) {
            LOG.warn("URI exception creating table: {},", table, e);
        } catch (StorageException e) {
            LOG.warn("Error generating TableClient: {}", table, e);
        }
        throw new IllegalStateException("Could not create table: " + table);
    }

    public void create(AzureTableName table) {
        try {
            tableRefrence(table).createIfNotExists();
            return;
        } catch (StorageException e) {
            LOG.warn("Error creating table: {}", table, e);
        }
        throw new IllegalStateException("Could not create table: " + table);
    }

    public boolean insert(TableType entity) {
        try {
            return tableRefrence(entity.getAzureTableName())
                    .execute(TableOperation.insert(entity))
                    .getHttpStatusCode() == Response.Status.NO_CONTENT.getStatusCode();
        } catch (StorageException e) {
            LOG.warn("Error performing operation on Storage service", e);
        }
        throw new IllegalStateException("Error adding data in table " + entity.getAzureTableName());
    }

    public boolean insertOrReplace(TableType entity) {
        try {
            final TableResult tableResult = tableRefrence(entity.getAzureTableName())
                    .execute(TableOperation.insertOrReplace(entity));
            switch (Response.Status.fromStatusCode(tableResult.getHttpStatusCode())) {
                case CREATED:
                case NO_CONTENT:
                    return true;
                default:
                    return false;
            }
        } catch (StorageException e) {
            LOG.warn("Error performing operation on Storage service", e);
        }
        throw new IllegalStateException("Error insertOrReplace in table " + entity.getAzureTableName());
    }

    public <EntityType extends TableServiceEntity> Optional<EntityType> retrieve(TableKey tableKey) {
        try {
            return Optional.ofNullable(tableRefrence(tableKey.getTable())
                    .execute(TableOperation.retrieve(tableKey.getPartitionKey(), tableKey.getRowKey(), tableKey.getEntityClass()))
                    .getResultAsType());
        } catch (StorageException e) {
            LOG.warn("Error retrieving entity from table: {}", tableKey.getTable(), e);
        }
        return Optional.empty();
    }

    public boolean update(TableType entity) {
        try {
            return tableRefrence(entity.getAzureTableName())
                    .execute(TableOperation.replace(entity))
                    .getHttpStatusCode() == Response.Status.NO_CONTENT.getStatusCode();
        } catch (StorageException e) {
            LOG.warn("Error updating row in table: {}", entity.getAzureTableName(), e);
        }
        throw new IllegalStateException("Error updating data in table: " + entity.getAzureTableName());
    }

    public boolean destroy(AzureTableName azureTableName) {
        try {
            return tableRefrence(azureTableName).deleteIfExists();
        } catch (StorageException e) {
            LOG.warn("Error deleting table from Azure", e);
        }
        throw new IllegalStateException("Unable to destroy table " + azureTableName);
    }

    public boolean exists(AzureTableName azureTableName) {
        try {
            return tableRefrence(azureTableName).exists();
        } catch (StorageException e) {
            LOG.warn("Error accessing azure table", e);
        }
        throw new IllegalStateException("Error verifying if table " + azureTableName + " exists");
    }

    public <EntityType extends TableEntity> ImmutableList<EntityType> search(AzureTableName azureTableName,
                                                                             TableQuery<EntityType> query) {
        return ImmutableList.copyOf(tableRefrence(azureTableName).execute(query));
    }

    public Iterable<String> listTables() {
        return cloudTableClient.listTables();
    }

    public boolean remove(TableType entity) {
        try {
            return tableRefrence(entity.getAzureTableName())
                    .execute(TableOperation.delete(entity))
                    .getHttpStatusCode() == Response.Status.NO_CONTENT.getStatusCode();
        } catch (StorageException e) {
            LOG.warn("Error updating row in table: {}", entity.getAzureTableName(), e);
        }
        throw new IllegalStateException("Error updating data in table: " + entity.getAzureTableName());
    }
}
