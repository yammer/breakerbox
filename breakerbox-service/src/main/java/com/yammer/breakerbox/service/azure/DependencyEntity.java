package com.yammer.breakerbox.service.azure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.microsoft.windowsazure.services.table.client.TableServiceEntity;
import com.yammer.azure.core.AzureTableName;
import com.yammer.azure.core.TableKey;
import com.yammer.azure.core.TableType;
import com.yammer.breakerbox.service.core.DependencyTableEntry;
import com.yammer.breakerbox.service.core.DependencyId;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.validation.Validator;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean to represent the DependencyEntity table.
 */
public class DependencyEntity extends TableType implements TableKey {
    private static final ObjectMapper OBJECTMAPPER = new ObjectMapperFactory().build();
    private static final Validator VALIDATOR = new Validator();
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEntity.class);

    private String tenacityConfigurationAsString;
    private String user;

    private DependencyEntity(DependencyId dependencyId, long timeStamp, String user, String configuration) {
        super(TableId.DEPENDENCY);
        this.partitionKey = dependencyId.getId();
        this.rowKey = String.valueOf(timeStamp);
        this.tenacityConfigurationAsString = configuration;
        this.user = user;
    }

    public static DependencyEntity build(DependencyId dependencyId, DependencyTableEntry record){
        try {
            final String configurationAsString = OBJECTMAPPER.writeValueAsString(record.getConfiguration());
            return new DependencyEntity(dependencyId, record.getTimestamp(), record.getUser(), configurationAsString);
        } catch (JsonProcessingException err) {
            LOGGER.warn("Could not convert TenacityConfiguration to json", err);
            throw new RuntimeException(err);
        }
    }

    public Optional<DependencyTableEntry> getDependencyTableEntry() {
        try {
            final TenacityConfiguration dependencyConfiguration = OBJECTMAPPER.readValue(tenacityConfigurationAsString, TenacityConfiguration.class);
            final ImmutableList<String> validationErrors = VALIDATOR.validate(dependencyConfiguration);
            if (!validationErrors.isEmpty()) {
                LOGGER.warn("Failed to validate TenacityConfiguration", validationErrors.toString());
            }
            return Optional.of(DependencyTableEntry.create(Long.valueOf(rowKey), user, dependencyConfiguration));
        } catch (Exception err) {
            LOGGER.warn("Failed to parse TenacityConfiguration", err);
        }
        return Optional.absent();
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

        if (!tenacityConfigurationAsString.equals(that.tenacityConfigurationAsString)) return false;
        if (!user.equals(that.user)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + tenacityConfigurationAsString.hashCode();
        result = 31 * result + user.hashCode();
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
    public String getUser() {
        return user;
    }

    @Deprecated
    public void setUser(String user) {
        this.user = user;
    }
}
