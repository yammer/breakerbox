package com.yammer.breakerbox.service.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.net.HostAndPort;
import com.yammer.breakerbox.azure.AzureTableConfiguration;
import com.yammer.breakerbox.jdbi.JdbiConfiguration;
import com.yammer.breakerbox.turbine.config.RancherInstanceConfiguration;
import com.yammer.dropwizard.authenticator.LdapConfiguration;
import com.yammer.tenacity.core.config.BreakerboxConfiguration;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BreakerboxServiceConfiguration extends Configuration {
    @NotNull @UnwrapValidatedValue(false) @Valid
    private final Optional<AzureTableConfiguration> azure;

    @NotNull @Valid
    private final JerseyClientConfiguration tenacityClient;

    @NotNull @Valid
    private final TenacityConfiguration breakerboxServicesPropertyKeys;

    @NotNull @Valid
    private final TenacityConfiguration breakerboxServicesConfiguration;

    @NotNull @Valid
    private final BreakerboxConfiguration breakerboxConfiguration;

    @NotNull @Valid
    private final Path turbine;

    @NotNull @Valid @UnwrapValidatedValue(false) @JsonProperty("ldap")
    private Optional<LdapConfiguration> ldapConfiguration = Optional.absent();

    @NotNull @Valid
    private ArchaiusOverrideConfiguration archaiusOverride;

    @NotNull @UnwrapValidatedValue(false) @Valid @JsonProperty("database")
    private Optional<JdbiConfiguration> jdbiConfiguration = Optional.absent();

    private List<String> metaClusters = Collections.emptyList();

    /* Useful if you are Breakerbox is behind a proxy and not at localhost:8080 */
    @NotNull @Valid
    private HostAndPort breakerboxHostAndPort;

    @NotNull
    private String defaultDashboard;

    @NotNull @UnwrapValidatedValue(false)
    private Optional<String> instanceDiscoveryClass;

    @NotNull @UnwrapValidatedValue(false)
    private final Optional<RancherInstanceConfiguration> rancherInstanceConfiguration;

    @JsonCreator
    public BreakerboxServiceConfiguration(@JsonProperty("azure") AzureTableConfiguration azure,
                                          @JsonProperty("tenacityClient") JerseyClientConfiguration tenacityClientConfiguration,
                                          @JsonProperty("breakerboxServicesPropertyKeys") TenacityConfiguration breakerboxServicesPropertyKeys,
                                          @JsonProperty("breakerboxServicesConfiguration") TenacityConfiguration breakerboxServicesConfiguration,
                                          @JsonProperty("breakerbox") BreakerboxConfiguration breakerboxConfiguration,
                                          @JsonProperty("ldap") LdapConfiguration ldapConfiguration,
                                          @JsonProperty("archaiusOverride") ArchaiusOverrideConfiguration archaiusOverride,
                                          @JsonProperty("database") JdbiConfiguration jdbiConfiguration,
                                          @JsonProperty("breakerboxHostAndPort") HostAndPort breakerboxHostAndPort,
                                          @JsonProperty("defaultDashboard") String defaultDashboard,
                                          @JsonProperty("turbine") Path turbine,
                                          @JsonProperty("instanceDiscoveryClass") String instanceDiscoveryClass,
                                          @JsonProperty("rancherDiscovery") RancherInstanceConfiguration rancherInstanceConfiguration) {
        this.azure = Optional.fromNullable(azure);
        this.tenacityClient = tenacityClientConfiguration;
        this.breakerboxServicesPropertyKeys = Optional.fromNullable(breakerboxServicesPropertyKeys).or(new TenacityConfiguration());
        this.breakerboxServicesConfiguration = Optional.fromNullable(breakerboxServicesConfiguration).or(new TenacityConfiguration());
        this.breakerboxConfiguration = breakerboxConfiguration;
        this.ldapConfiguration = Optional.fromNullable(ldapConfiguration);
        this.archaiusOverride = Optional.fromNullable(archaiusOverride).or(new ArchaiusOverrideConfiguration());
        this.jdbiConfiguration = Optional.fromNullable(jdbiConfiguration);
        this.breakerboxHostAndPort = Optional.fromNullable(breakerboxHostAndPort).or(HostAndPort.fromParts("localhost", 8080));
        this.defaultDashboard = Optional.fromNullable(defaultDashboard).or("production");
        this.turbine = Optional.fromNullable(turbine).or(Paths.get("breakerbox-instances.yml"));
        this.instanceDiscoveryClass = Optional.fromNullable(instanceDiscoveryClass)
                .or(Optional.fromNullable(System.getProperty("InstanceDiscovery.impl")));
        this.rancherInstanceConfiguration = Optional.fromNullable(rancherInstanceConfiguration);
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

    public Path getTurbine() {
        return turbine;
    }

    public void setArchaiusOverride(ArchaiusOverrideConfiguration archaiusOverride) {
        this.archaiusOverride = archaiusOverride;
    }

    public void setLdapConfiguration(LdapConfiguration ldapConfiguration) {
        this.ldapConfiguration = Optional.fromNullable(ldapConfiguration);
    }

    @JsonProperty("database")
    public Optional<JdbiConfiguration> getJdbiConfiguration() {
        return jdbiConfiguration;
    }

    public void setJdbiConfiguration(JdbiConfiguration jdbiConfiguration) {
        this.jdbiConfiguration = Optional.fromNullable(jdbiConfiguration);
    }

    public HostAndPort getBreakerboxHostAndPort() {
        return breakerboxHostAndPort;
    }

    public String getDefaultDashboard() {
        return defaultDashboard;
    }

    public List<String> getMetaClusters() {
        return metaClusters;
    }

    public Optional<String> getInstanceDiscoveryClass() {
        return instanceDiscoveryClass;
    }

    public void setInstanceDiscoveryClass(String instanceDiscoveryClass) {
        this.instanceDiscoveryClass = Optional.fromNullable(instanceDiscoveryClass);
    }

    public Optional<RancherInstanceConfiguration> getRancherInstanceConfiguration() {
        return rancherInstanceConfiguration;
    }

    @Override
    public int hashCode() {
        return Objects.hash(azure, tenacityClient, breakerboxServicesPropertyKeys, breakerboxServicesConfiguration, breakerboxConfiguration, turbine, ldapConfiguration, archaiusOverride, jdbiConfiguration, metaClusters, breakerboxHostAndPort, defaultDashboard, instanceDiscoveryClass, rancherInstanceConfiguration);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final BreakerboxServiceConfiguration other = (BreakerboxServiceConfiguration) obj;
        return Objects.equals(this.azure, other.azure)
                && Objects.equals(this.tenacityClient, other.tenacityClient)
                && Objects.equals(this.breakerboxServicesPropertyKeys, other.breakerboxServicesPropertyKeys)
                && Objects.equals(this.breakerboxServicesConfiguration, other.breakerboxServicesConfiguration)
                && Objects.equals(this.breakerboxConfiguration, other.breakerboxConfiguration)
                && Objects.equals(this.turbine, other.turbine)
                && Objects.equals(this.ldapConfiguration, other.ldapConfiguration)
                && Objects.equals(this.archaiusOverride, other.archaiusOverride)
                && Objects.equals(this.jdbiConfiguration, other.jdbiConfiguration)
                && Objects.equals(this.metaClusters, other.metaClusters)
                && Objects.equals(this.breakerboxHostAndPort, other.breakerboxHostAndPort)
                && Objects.equals(this.defaultDashboard, other.defaultDashboard)
                && Objects.equals(this.instanceDiscoveryClass, other.instanceDiscoveryClass)
                && Objects.equals(this.rancherInstanceConfiguration, other.rancherInstanceConfiguration);
    }
}
