package com.yammer.avalanche.service.azure;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.microsoft.windowsazure.services.table.client.TableServiceEntity;
import com.yammer.avalanche.service.core.DependencyId;
import com.yammer.avalanche.service.core.ServiceId;
import com.yammer.azure.core.AzureTableName;
import com.yammer.azure.core.TableKey;
import com.yammer.azure.core.TableType;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.validation.Validator;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenacityEntity extends TableType {
    private String tenacityConfigurationAsString;
    private static final ObjectMapper OBJECTMAPPER = new ObjectMapperFactory().build();
    private static final Validator VALIDATOR = new Validator();
    private static final Logger LOGGER = LoggerFactory.getLogger(TenacityEntity.class);

    private TenacityEntity(String tenacityConfigurationAsString, ServiceId serviceId, DependencyId dependencyId) throws JsonProcessingException {
        super(TableId.TENACITYSERVICES);
        this.tenacityConfigurationAsString = tenacityConfigurationAsString;
        this.partitionKey = serviceId.toString();
        this.rowKey = dependencyId.toString();
    }

    public static TenacityEntity build(TenacityConfiguration tenacityConfiguration,
                                       ServiceId serviceId,
                                       DependencyId dependencyId) {
        try {
            return new TenacityEntity(
                    OBJECTMAPPER.writeValueAsString(tenacityConfiguration),
                    serviceId,
                    dependencyId);
        } catch (Exception err) {
            LOGGER.warn("Could not convert TenacityConfiguration to json", err);
            throw new RuntimeException(err);
        }
    }

    public TenacityEntity using(TenacityConfiguration configuration) {
        return TenacityEntity.build(configuration, getServiceId(), getDependencyId());
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

    public Key key() {
        return new Key(getRowKey(), getPartitionKey());
    }

    @Deprecated
    public String getTenacityConfigurationAsString() {
        return tenacityConfigurationAsString;
    }

    @Deprecated
    public void setTenacityConfigurationAsString(String tenacityConfigurationAsString) {
        this.tenacityConfigurationAsString = tenacityConfigurationAsString;
    }

    public ServiceId getServiceId() {
        return ServiceId.from(getPartitionKey());
    }

    public DependencyId getDependencyId() {
        return DependencyId.from(getRowKey());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TenacityEntity that = (TenacityEntity) o;

        if (partitionKey != null ? !partitionKey.equals(that.partitionKey) : that.partitionKey != null) return false;
        if (rowKey != null ? !rowKey.equals(that.rowKey) : that.rowKey != null) return false;
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
        private final String rowKey;
        private final String partitionKey;

        public Key(String rowKey, String partitionKey) {
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
            return TenacityEntity.class;
        }

        @Override
        public AzureTableName getTable() {
            return TableId.TENACITYSERVICES;
        }
    }
}
