package com.yammer.breakerbox.azure;

import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.RetryLinearRetry;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;

public class TableClientFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(TableClientFactory.class);
    private final AzureTableConfiguration azureTableConfiguration;

    public TableClientFactory(AzureTableConfiguration azureTableConfiguration) {
        this.azureTableConfiguration = checkNotNull(azureTableConfiguration, "azureTableConfiguration cannot be null");
    }

    public TableClient create() {
        try {
            final CloudStorageAccount storageAccount =
                    new CloudStorageAccount(azureTableConfiguration.getStorageCredentialsAccountAndKey(), true);
            final CloudTableClient cloudTableClient = storageAccount.createCloudTableClient();
            cloudTableClient.setRetryPolicyFactory(
                    new RetryLinearRetry(
                            (int) azureTableConfiguration.getRetryInterval().toMilliseconds(),
                            azureTableConfiguration.getRetryAttempts()));
            cloudTableClient.setTimeoutInMs((int) azureTableConfiguration.getConnectionTimeout().toMilliseconds());
            return new TableClient(cloudTableClient);
        } catch (URISyntaxException err) {
            LOGGER.error("Failed to create a TableClient", err);
            throw new IllegalArgumentException(err);
        }
    }
}