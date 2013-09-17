package com.yammer.breakerbox.service.azure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.microsoft.windowsazure.services.table.client.TableServiceEntity;
import com.yammer.azure.core.AzureTableName;
import com.yammer.azure.core.TableKey;
import com.yammer.azure.core.TableType;
import com.yammer.breakerbox.service.core.DependencyId;
import com.yammer.breakerbox.service.core.ServiceId;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.validation.Validator;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean to represent the Dependency table.
 * This is for looking up time-based configurations on a per-key basis. It is assumed that you know which
 * Dependency Key you wish to use before accessing this table.
 */
public class DependencyEntity extends TableType implements TableKey {
    private static final ObjectMapper OBJECTMAPPER = new ObjectMapperFactory().build();
    private static final Validator VALIDATOR = new Validator();
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEntity.class);

    private String tenacityConfigurationAsString;
    private String user;
    private String serviceName;

    private DependencyEntity(DependencyId dependencyId,
                             long timeStamp,
                             String userName,
                             String configuration,
                             ServiceId serviceId) {
        super(TableId.DEPENDENCY);
        this.partitionKey = dependencyId.getId();
        this.rowKey = String.valueOf(timeStamp);
        this.tenacityConfigurationAsString = configuration;
        this.user = userName;
        this.serviceName = serviceId.getId();
    }

    @VisibleForTesting
    static DependencyEntity build(DependencyId dependencyId, long timeStamp, String userName, ServiceId serviceId) {
        return build(dependencyId, timeStamp, userName, new TenacityConfiguration(), serviceId);
    }

    public static DependencyEntity build(DependencyId dependencyId,
                                         String username,
                                         TenacityConfiguration tenacityConfiguration,
                                         ServiceId serviceId) {
        return build(dependencyId, System.currentTimeMillis(), username, tenacityConfiguration, serviceId);
    }

    public static DependencyEntity build(DependencyId dependencyId,
                                         long timeStamp,
                                         String userName,
                                         TenacityConfiguration configuration,
                                         ServiceId serviceId) {
        try {
            final String configurationAsString = OBJECTMAPPER.writeValueAsString(configuration);
            return new DependencyEntity(dependencyId, timeStamp, userName, configurationAsString, serviceId);
        } catch (JsonProcessingException err) {
            LOGGER.warn("Could not convert TenacityConfiguration to json", err);
            throw new RuntimeException(err);
        }
    }

    public Optional<TenacityConfiguration> getConfiguration() {
        try {
            final TenacityConfiguration dependencyConfiguration = OBJECTMAPPER.readValue(tenacityConfigurationAsString, TenacityConfiguration.class);
            final ImmutableList<String> validationErrors = VALIDATOR.validate(dependencyConfiguration);
            if (!validationErrors.isEmpty()) {
                LOGGER.warn("Failed to validate TenacityConfiguration", validationErrors.toString());
            }
            return Optional.of(dependencyConfiguration);
        } catch (Exception err) {
            LOGGER.warn("Failed to parse TenacityConfiguration", err);
        }
        return Optional.absent();
    }

    public long getConfigurationTimestamp() {
        return Long.parseLong(rowKey);
    }

    public static TenacityConfiguration defaultConfiguration() {
        return new TenacityConfiguration();
    }

    public ServiceId getServiceId() {
        return ServiceId.from(serviceName);
    }

    public DependencyId getDependencyId() {
        return DependencyId.from(partitionKey);
    }

    @Override
    public Class<? extends TableServiceEntity> getEntityClass() {
        return DependencyEntity.class;
    }

    @Override
    public AzureTableName getTable() {
        return TableId.DEPENDENCY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DependencyEntity that = (DependencyEntity) o;

        if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) return false;
        if (tenacityConfigurationAsString != null ? !tenacityConfigurationAsString.equals(that.tenacityConfigurationAsString) : that.tenacityConfigurationAsString != null)
            return false;
        if (user != null ? !user.equals(that.user) : that.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (tenacityConfigurationAsString != null ? tenacityConfigurationAsString.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
        return result;
    }

    /**
     * For Azure bean serialization via reflection
     */

    @Deprecated
    public DependencyEntity() {
        super(TableId.DEPENDENCY);
    }

    @Deprecated
    public String getTenacityConfigurationAsString() {
        return tenacityConfigurationAsString;
    }

    @Deprecated
    public void setTenacityConfigurationAsString(String tenacityConfigurationAsString) {
        this.tenacityConfigurationAsString = tenacityConfigurationAsString;
    }

    @Deprecated
    public String getServiceName() {
        return serviceName;
    }

    @Deprecated
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Getter for user field. <br/>
     * NOTE: This method is exposed, but is also used for bean serialization. Don't remove this.
     */
    public String getUser() {
        return user;
    }

    @Deprecated
    public void setUser(String user) {
        this.user = user;
    }
}
