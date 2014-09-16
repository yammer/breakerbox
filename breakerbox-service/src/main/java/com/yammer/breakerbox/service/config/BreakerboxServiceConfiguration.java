package com.yammer.breakerbox.service.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.net.HostAndPort;
import com.yammer.breakerbox.azure.AzureTableConfiguration;
import com.yammer.breakerbox.jdbi.JdbiConfiguration;
import com.yammer.dropwizard.authenticator.LdapConfiguration;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class BreakerboxServiceConfiguration extends Configuration {
    @NotNull @Valid
    private final Optional<AzureTableConfiguration> azure;

    @NotNull @Valid
    private final JerseyClientConfiguration tenacityClient;

    @NotNull @Valid
    private final TenacityConfiguration breakerboxServicesPropertyKeys;

    @NotNull @Valid
    private final TenacityConfiguration breakerboxServicesConfiguration;

    @NotNull @Valid
    private final BreakerboxConfiguration breakerboxConfiguration;


    @NotNull @Valid @JsonProperty("ldap")
    private Optional<LdapConfiguration> ldapConfiguration = Optional.absent();

    @NotNull @Valid
    private ArchaiusOverrideConfiguration archaiusOverride;

    @NotNull @Valid @JsonProperty("database")
    private Optional<JdbiConfiguration> jdbiConfiguration = Optional.absent();

    /* Useful if you are Breakerbox is behind a proxy and not at localhost:8080 */
    @NotNull @Valid
    private HostAndPort breakerboxHostAndPort;

    @JsonCreator
    public BreakerboxServiceConfiguration(@JsonProperty("azure") AzureTableConfiguration azure,
                                          @JsonProperty("tenacityClient") JerseyClientConfiguration tenacityClientConfiguration,
                                          @JsonProperty("breakerboxServicesPropertyKeys") TenacityConfiguration breakerboxServicesPropertyKeys,
                                          @JsonProperty("breakerboxServicesConfiguration") TenacityConfiguration breakerboxServicesConfiguration,
                                          @JsonProperty("breakerbox") BreakerboxConfiguration breakerboxConfiguration,
                                          @JsonProperty("ldap") LdapConfiguration ldapConfiguration,
                                          @JsonProperty("archaiusOverride") ArchaiusOverrideConfiguration archaiusOverride,
                                          @JsonProperty("database") JdbiConfiguration jdbiConfiguration,
                                          @JsonProperty("breakerboxHostAndPort") HostAndPort breakerboxHostAndPort) {
        this.azure = Optional.fromNullable(azure);
        this.tenacityClient = tenacityClientConfiguration;
        this.breakerboxServicesPropertyKeys = Optional.fromNullable(breakerboxServicesPropertyKeys).or(new TenacityConfiguration());
        this.breakerboxServicesConfiguration = Optional.fromNullable(breakerboxServicesConfiguration).or(new TenacityConfiguration());
        this.breakerboxConfiguration = breakerboxConfiguration;
        this.ldapConfiguration = Optional.fromNullable(ldapConfiguration);
        this.archaiusOverride = Optional.fromNullable(archaiusOverride).or(new ArchaiusOverrideConfiguration());
        this.jdbiConfiguration = Optional.fromNullable(jdbiConfiguration);
        this.breakerboxHostAndPort = Optional.fromNullable(breakerboxHostAndPort).or(HostAndPort.fromParts("localhost", 8080));
    }

    public Optional<AzureTableConfiguration> getAzure() {
        return azure;
    }

    public JerseyClientConfiguration getTenacityClient() {
        return tenacityClient;
    }

    public TenacityConfiguration getBreakerboxServicesPropertyKeys() {
        return breakerboxServicesPropertyKeys;
    }

    public TenacityConfiguration getBreakerboxServicesConfiguration() {
        return breakerboxServicesConfiguration;
    }

    public BreakerboxConfiguration getBreakerboxConfiguration() {
        return breakerboxConfiguration;
    }

    @JsonProperty("ldap")
    public Optional<LdapConfiguration> getLdapConfiguration() {
        return ldapConfiguration;
    }

    public ArchaiusOverrideConfiguration getArchaiusOverride() {
        return archaiusOverride;
    }

    public void setArchaiusOverride(ArchaiusOverrideConfiguration archaiusOverride) {
        this.archaiusOverride = archaiusOverride;
    }

    public void setLdapConfiguration(Optional<LdapConfiguration> ldapConfiguration) {
        this.ldapConfiguration = ldapConfiguration;
    }

    @JsonProperty("database")
    public Optional<JdbiConfiguration> getJdbiConfiguration() {
        return jdbiConfiguration;
    }

    public void setJdbiConfiguration(Optional<JdbiConfiguration> jdbiConfiguration) {
        this.jdbiConfiguration = jdbiConfiguration;
    }

    public HostAndPort getBreakerboxHostAndPort() {
        return breakerboxHostAndPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BreakerboxServiceConfiguration that = (BreakerboxServiceConfiguration) o;

        if (!archaiusOverride.equals(that.archaiusOverride)) return false;
        if (!azure.equals(that.azure)) return false;
        if (!breakerboxConfiguration.equals(that.breakerboxConfiguration)) return false;
        if (!breakerboxHostAndPort.equals(that.breakerboxHostAndPort)) return false;
        if (!breakerboxServicesConfiguration.equals(that.breakerboxServicesConfiguration)) return false;
        if (!breakerboxServicesPropertyKeys.equals(that.breakerboxServicesPropertyKeys)) return false;
        if (!jdbiConfiguration.equals(that.jdbiConfiguration)) return false;
        if (!ldapConfiguration.equals(that.ldapConfiguration)) return false;
        if (!tenacityClient.equals(that.tenacityClient)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = azure.hashCode();
        result = 31 * result + tenacityClient.hashCode();
        result = 31 * result + breakerboxServicesPropertyKeys.hashCode();
        result = 31 * result + breakerboxServicesConfiguration.hashCode();
        result = 31 * result + breakerboxConfiguration.hashCode();
        result = 31 * result + ldapConfiguration.hashCode();
        result = 31 * result + archaiusOverride.hashCode();
        result = 31 * result + jdbiConfiguration.hashCode();
        result = 31 * result + breakerboxHostAndPort.hashCode();
        return result;
    }
}