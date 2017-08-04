package com.yammer.breakerbox.azure;

import com.google.common.primitives.Ints;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.RetryLinearRetry;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableRequestOptions;
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
            final TableRequestOptions defaultOptions = new TableRequestOptions();
            defaultOptions.setRetryPolicyFactory(new RetryLinearRetry(
                    Ints.checkedCast(azureTableConfiguration.getRetryInterval().toMilliseconds()),
                    azureTableConfiguration.getRetryAttempts()));
            defaultOptions.setTimeoutIntervalInMs(Ints.checkedCast(azureTableConfiguration.getTimeout().toMilliseconds()));
            cloudTableClient.setDefaultRequestOptions(defaultOptions);
            return new TableClient(cloudTableClient);
        } catch (URISyntaxException err) {
            LOGGER.error("Failed to create a TableClient", err);
            throw new IllegalArgumentException(err);
        }
    }
}