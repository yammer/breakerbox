package com.yammer.breakerbox.azure.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.table.TableServiceEntity;
import com.yammer.breakerbox.azure.core.AzureTableName;
import com.yammer.breakerbox.azure.core.TableId;
import com.yammer.breakerbox.azure.core.TableKey;
import com.yammer.breakerbox.azure.core.TableType;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import io.dropwizard.jackson.Jackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Optional;
import java.util.Set;

/**
 * Bean to represent the Dependency table.
 * This is for looking up time-based configurations on a per-key basis. It is assumed that you know which
 * Dependency Key you wish to use before accessing this table.
 */
public class DependencyEntity extends TableType implements TableKey {
    private static final ObjectMapper OBJECTMAPPER = Jackson.newObjectMapper();
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
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
            final Set<?> validationErrors = VALIDATOR.validate(dependencyConfiguration);
            if (!validationErrors.isEmpty()) {
                LOGGER.warn("Failed to validate TenacityConfiguration", validationErrors.toString());
            }
            return Optional.of(dependencyConfiguration);
        } catch (Exception err) {
            LOGGER.warn("Failed to parse TenacityConfiguration", err);
        }
        return Optional.empty();
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
     * @deprecated kept for backward compatibility
     * For Azure bean serialization via reflection
     */

    @Deprecated
    public DependencyEntity() {
        super(TableId.DEPENDENCY);
    }

    /**
     * @deprecated kept for backward compatibility
     * @return
     */
    @Deprecated
    public String getTenacityConfigurationAsString() {
        return tenacityConfigurationAsString;
    }

    /**
     * @deprecated kept for backward compatibility
     * @param tenacityConfigurationAsString
     */
    @Deprecated
    public void setTenacityConfigurationAsString(String tenacityConfigurationAsString) {
        this.tenacityConfigurationAsString = tenacityConfigurationAsString;
    }

    /**
     * @deprecated kept for backward compatibility
     * @return
     */
    @Deprecated
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @deprecated kept for backward compatibility
     * @param serviceName
     */
    @Deprecated
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Getter for user field.
     * NOTE: This method is exposed, but is also used for bean serialization. Don't remove this.
     */
    public String getUser() {
        return user;
    }

    /**
     * @deprecated kept for backward compatibility
     * @param user
     */
    @Deprecated
    public void setUser(String user) {
        this.user = user;
    }
}
