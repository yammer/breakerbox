package com.yammer.breakerbox.service.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.yammer.azure.config.AzureTableConfiguration;
import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.tenacity.core.config.TenacityConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class BreakerboxConfiguration extends Configuration {
    @NotNull @Valid
    private final AzureTableConfiguration azure;

    @NotNull @Valid
    private final JerseyClientConfiguration tenacityClient;

    @NotNull @Valid
    private final TenacityConfiguration breakerboxServicesTenacity;

    @NotNull @Valid
    private final com.yammer.tenacity.core.config.BreakerboxConfiguration breakerboxConfiguration;

    @NotNull @Valid
    private final URI configProperties;

    @JsonCreator
    public BreakerboxConfiguration(@JsonProperty("azure") AzureTableConfiguration azure,
                                   @JsonProperty("tenacityClient") JerseyClientConfiguration tenacityClientConfiguration,
                                   @JsonProperty("breakerboxServicesTenacity") TenacityConfiguration breakerboxServicesTenacity,
                                   @JsonProperty("breakerbox") com.yammer.tenacity.core.config.BreakerboxConfiguration breakerboxConfiguration,
                                   @JsonProperty("configProperties") URI configProperties) {
        this.azure = azure;
        this.tenacityClient = tenacityClientConfiguration;
        this.breakerboxServicesTenacity = Optional.fromNullable(breakerboxServicesTenacity).or(new TenacityConfiguration());
        this.breakerboxConfiguration = breakerboxConfiguration;
        this.configProperties = configProperties;
    }

    public AzureTableConfiguration getAzure() {
        return azure;
    }

    public JerseyClientConfiguration getTenacityClient() {
        return tenacityClient;
    }

    public TenacityConfiguration getBreakerboxServicesTenacity() {
        return breakerboxServicesTenacity;
    }

    public com.yammer.tenacity.core.config.BreakerboxConfiguration getBreakerboxConfiguration() {
        return breakerboxConfiguration;
    }

    public URI getConfigProperties() {
        return configProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BreakerboxConfiguration that = (BreakerboxConfiguration) o;

        if (!azure.equals(that.azure)) return false;
        if (!breakerboxConfiguration.equals(that.breakerboxConfiguration)) return false;
        if (!breakerboxServicesTenacity.equals(that.breakerboxServicesTenacity)) return false;
        if (!configProperties.equals(that.configProperties)) return false;
        if (!tenacityClient.equals(that.tenacityClient)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = azure.hashCode();
        result = 31 * result + tenacityClient.hashCode();
        result = 31 * result + breakerboxServicesTenacity.hashCode();
        result = 31 * result + breakerboxConfiguration.hashCode();
        result = 31 * result + configProperties.hashCode();
        return result;
    }
}