package com.yammer.breakerbox.service.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.azure.config.AzureTableConfiguration;
import com.yammer.dropwizard.config.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class BreakerboxConfiguration extends Configuration {
    @NotNull @Valid
    private final AzureTableConfiguration azure;

    @JsonCreator
    public BreakerboxConfiguration(@JsonProperty("azure") AzureTableConfiguration azure) {
        this.azure = azure;
    }

    public AzureTableConfiguration getAzure() {
        return azure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BreakerboxConfiguration that = (BreakerboxConfiguration) o;

        if (!azure.equals(that.azure)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return azure.hashCode();
    }
}