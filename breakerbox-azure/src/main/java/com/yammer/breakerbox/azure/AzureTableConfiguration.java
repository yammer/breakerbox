package com.yammer.breakerbox.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.windowsazure.services.core.storage.StorageCredentialsAccountAndKey;
import com.yammer.dropwizard.util.Duration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

public class AzureTableConfiguration {

    @NotNull @Valid
    private final StorageCredentialsAccountAndKey storageCredentialsAccountAndKey;
    @NotNull @Valid
    private final Duration connectionTimeout;
    @NotNull @Valid
    private final Duration retryInterval;
    @Valid
    private final int retryAttempts;

    @JsonCreator
    public AzureTableConfiguration(@JsonProperty("accountName") String accountName,
                                   @JsonProperty("accountKey") String accountKey,
                                   @JsonProperty("connectionTimeout")Duration connectionTimeout,
                                   @JsonProperty("retryInterval") Duration retryInterval,
                                   @JsonProperty("retryAttempts") int retryAttempts) {
        this.retryInterval = checkNotNull(retryInterval, "retryInterval cannot be null");
        this.retryAttempts = checkNotNull(retryAttempts, "retryAttempts cannot be null");
        this.connectionTimeout = checkNotNull(connectionTimeout, "connectionTimeout cannot be null");
        this.storageCredentialsAccountAndKey =
                new StorageCredentialsAccountAndKey(
                        checkNotNull(accountName, "accountName cannot be null"),
                        checkNotNull(accountKey, "accountKey cannot be null"));
    }

    @JsonIgnore
    public StorageCredentialsAccountAndKey getStorageCredentialsAccountAndKey() {
        return storageCredentialsAccountAndKey;
    }

    public Duration getRetryInterval() {
        return retryInterval;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }
}