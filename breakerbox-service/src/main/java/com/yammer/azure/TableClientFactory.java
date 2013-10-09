package com.yammer.azure;

import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.RetryLinearRetry;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;

import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;

public class TableClientFactory {

    private final AzureTableConfiguration azureTableConfiguration;

    public TableClientFactory(AzureTableConfiguration azureTableConfiguration) {
        this.azureTableConfiguration = checkNotNull(azureTableConfiguration, "azureTableConfiguration cannot be null");
    }

    public TableClient create() throws URISyntaxException {
        final CloudStorageAccount storageAccount =
                new CloudStorageAccount(azureTableConfiguration.getStorageCredentialsAccountAndKey(), true);
        final CloudTableClient cloudTableClient = storageAccount.createCloudTableClient();
        cloudTableClient.setRetryPolicyFactory(
                new RetryLinearRetry(
                        (int) azureTableConfiguration.getRetryInterval().toMilliseconds(),
                        azureTableConfiguration.getRetryAttempts()));
        cloudTableClient.setTimeoutInMs((int) azureTableConfiguration.getConnectionTimeout().toMilliseconds());
        return new TableClient(cloudTableClient);
    }
}