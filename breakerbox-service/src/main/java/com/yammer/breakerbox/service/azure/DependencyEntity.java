package com.yammer.breakerbox.service.azure;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.microsoft.windowsazure.services.table.client.TableServiceEntity;
import com.yammer.azure.core.AzureTableName;
import com.yammer.azure.core.TableKey;
import com.yammer.azure.core.TableType;
import com.yammer.breakerbox.service.core.DependencyId;
import com.yammer.breakerbox.service.core.EnvironmentId;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.validation.Validator;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DependencyEntity extends TableType {
    private String tenacityConfigurationAsString;
    private static final ObjectMapper OBJECTMAPPER = new ObjectMapperFactory().build();
    private static final Validator VALIDATOR = new Validator();
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyEntity.class);

    @Deprecated
    public DependencyEntity() {
        super(TableId.DEPENDENCIES);
    }

    private DependencyEntity(String tenacityConfigurationAsString, DependencyId dependencyId, EnvironmentId environmentId) {
        super(TableId.DEPENDENCIES);
        this.tenacityConfigurationAsString = tenacityConfigurationAsString;
        this.partitionKey = environmentId.getId();
        this.rowKey = dependencyId.getId();
    }

    public static DependencyEntity build(TenacityConfiguration tenacityConfiguration,
                                         EnvironmentId environmentId,
                                         DependencyId dependencyId) {
        try {
            return new DependencyEntity(
                    OBJECTMAPPER.writeValueAsString(tenacityConfiguration),
                    dependencyId,
                    environmentId);
        } catch (Exception err) {
            LOGGER.warn("Could not convert TenacityConfiguration to json", err);
            throw new RuntimeException(err);
        }
    }

    public DependencyEntity using(TenacityConfiguration configuration) {
        return DependencyEntity.build(configuration, getEnvironmentId(), getDependencyId());
    }

    public Optional<TenacityConfiguration> getTenacityConfiguration() {
        try {
            final TenacityConfiguration configuration = OBJECTMAPPER.readValue(tenacityConfigurationAsString, TenacityConfiguration.class);
            final ImmutableList<String> validationErrors = VALIDATOR.validate(configuration);
            if (!validationErrors.isEmpty()) {
                LOGGER.warn("Failed to validate TenacityConfiguration", validationErrors.toString());
            }
            return Optional.of(configuration);
        } catch (Exception err) {
            LOGGER.warn("Failed to parse TenacityConfiguration", err);
        }
        return Optional.absent();
    }

    public static Key key(EnvironmentId environmentId, DependencyId dependencyId) {
        return new Key(environmentId.getId(), dependencyId.getId());
    }

    public Key key() {
        return key(getEnvironmentId(), getDependencyId());
    }

    @Deprecated
    public String getTenacityConfigurationAsString() {
        return tenacityConfigurationAsString;
    }

    @Deprecated
    public void setTenacityConfigurationAsString(String tenacityConfigurationAsString) {
        this.tenacityConfigurationAsString = tenacityConfigurationAsString;
    }

    public EnvironmentId getEnvironmentId() {
        return EnvironmentId.from(getPartitionKey());
    }

    public DependencyId getDependencyId() {
        return DependencyId.from(getRowKey());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DependencyEntity that = (DependencyEntity) o;

        if (!tenacityConfigurationAsString.equals(that.tenacityConfigurationAsString)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + tenacityConfigurationAsString.hashCode();
        return result;
    }

    public static class Key implements TableKey {
        private final String partitionKey;
        private final String rowKey;

        public Key(String partitionKey, String rowKey) {
            this.rowKey = rowKey;
            this.partitionKey = partitionKey;
        }

        @Override
        public String getRowKey() {
            return rowKey;
        }

        @Override
        public String getPartitionKey() {
            return partitionKey;
        }

        @Override
        public Class<? extends TableServiceEntity> getEntityClass() {
            return DependencyEntity.class;
        }

        @Override
        public AzureTableName getTable() {
            return TableId.DEPENDENCIES;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (!partitionKey.equals(key.partitionKey)) return false;
            if (!rowKey.equals(key.rowKey)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = partitionKey.hashCode();
            result = 31 * result + rowKey.hashCode();
            return result;
        }
    }
}
