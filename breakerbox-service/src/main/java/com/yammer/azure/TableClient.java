package com.yammer.azure;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.*;
import com.yammer.azure.core.AzureTableName;
import com.yammer.azure.core.TableKey;
import com.yammer.azure.core.TableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;

public class TableClient {

    private final CloudTableClient cloudTableClient;
    private static final Logger LOG = LoggerFactory.getLogger(TableClient.class);

    public TableClient(CloudTableClient cloudTableClient) {
        this.cloudTableClient = checkNotNull(cloudTableClient, "cloudTableClient cannot be null");
    }

    public void create(AzureTableName table) {
        try {
            final CloudTable cloudTable = new CloudTable(table.toString(), cloudTableClient);
            cloudTable.createIfNotExist();
            return;
        } catch (URISyntaxException e) {
            LOG.warn("URI exception creating table", e);
        } catch (StorageException e) {
            LOG.warn("Error creating table: {}", table, e);
        }
        throw new IllegalStateException("Could not create table: " + table);
    }

    public boolean insert(TableType entity) {
        try {
            final TableResult tableResult = cloudTableClient.execute(
                    entity.getAzureTableName().toString(), TableOperation.insert(entity));
            return tableResult.getHttpStatusCode() == Response.Status.CREATED.getStatusCode();
        } catch (StorageException e) {
            LOG.warn("Error performing operation on Storage service", e);
        }
        throw new IllegalStateException("Error adding data in table " + entity.getAzureTableName());
    }

    public boolean insertOrReplace(TableType entity) {
        try {
            final TableResult tableResult = cloudTableClient.execute(
                    entity.getAzureTableName().toString(), TableOperation.insertOrReplace(entity));
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
            final TableResult tableResult = cloudTableClient.execute(
                    tableKey.getTable().toString(),
                    TableOperation.retrieve(
                            tableKey.getPartitionKey(), tableKey.getRowKey(), tableKey.getEntityClass()));
            final EntityType entityType = tableResult.getResultAsType();
            return Optional.fromNullable(entityType);
        } catch (StorageException e) {
            LOG.warn("Error retrieving entity from table: {}", tableKey.getTable(), e);
        }
        return Optional.absent();
    }

    public boolean update(TableType entity) {
        try {
            final TableResult result = cloudTableClient.execute(
                    entity.getAzureTableName().toString(),
                    TableOperation.replace(entity));
            return result.getHttpStatusCode() == Response.Status.NO_CONTENT.getStatusCode();
        } catch (StorageException e) {
            LOG.warn("Error updating row in table: {}", entity.getAzureTableName(), e);
        }
        throw new IllegalStateException("Error updating data in table: " + entity.getAzureTableName());
    }

    public boolean destroy(AzureTableName azureTableName) {
        try {
            final CloudTable cloudTable = new CloudTable(azureTableName.toString(), cloudTableClient);
            return cloudTable.deleteIfExists();
        } catch (URISyntaxException e) {
            LOG.warn("Invalid Azure table URL specified {}", azureTableName, e);
        } catch (StorageException e) {
            LOG.warn("Error deleting table from Azure", e);
        }
        throw new IllegalStateException("Unable to destroy table " + azureTableName);
    }

    public boolean exists(AzureTableName azureTableName) {
        try {
            final CloudTable cloudTable = new CloudTable(azureTableName.toString(), cloudTableClient);
            return cloudTable.exists();
        } catch (URISyntaxException e) {
            LOG.warn("Invalid Azure table URL specified {}", azureTableName, e);
        } catch (StorageException e) {
            LOG.warn("Error accessing azure table", e);
        }
        throw new IllegalStateException("Error verifying if table " + azureTableName + " exists");
    }

    public <EntityType extends TableEntity> ImmutableList<EntityType> search(TableQuery<EntityType> query) {
        return ImmutableList.copyOf(cloudTableClient.execute(query));
    }

    public Iterable<String> listTables() {
        return cloudTableClient.listTables();
    }

    public boolean remove(TableType entity) {
        try {
            final TableResult result = cloudTableClient.execute(
                    entity.getAzureTableName().toString(), TableOperation.delete(entity));
            return result.getHttpStatusCode() == Response.Status.NO_CONTENT.getStatusCode();
        } catch (StorageException e) {
            LOG.warn("Error updating row in table: {}", entity.getAzureTableName(), e);
        }
        throw new IllegalStateException("Error updating data in table: " + entity.getAzureTableName());
    }
}
