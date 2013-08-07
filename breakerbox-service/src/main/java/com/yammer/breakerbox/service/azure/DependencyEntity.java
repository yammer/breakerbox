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

public class DependencyEntity extends TableType implements TableKey {
    private String tenacityConfigurationAsString;
    private static final ObjectMapper OBJECTMAPPER = new ObjectMapperFactory().build();
    private static final Validator VALIDATOR = new Validator();
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyEntity.class);

    private DependencyEntity(DependencyId dependencyId,
                             EnvironmentId environmentId,
                             String tenacityConfigurationAsString) {
        super(TableId.DEPENDENCIES);
        this.partitionKey = dependencyId.getId();
        this.rowKey = environmentId.getId();
        this.tenacityConfigurationAsString = tenacityConfigurationAsString;
    }

    public static DependencyEntity build(DependencyId dependencyId,
                                         EnvironmentId environmentId) {
        return build(dependencyId, environmentId, new TenacityConfiguration());
    }

    public static DependencyEntity build(DependencyId dependencyId,
                                         EnvironmentId environmentId,
                                         TenacityConfiguration tenacityConfiguration) {
        try {
            return new DependencyEntity(
                    dependencyId,
                    environmentId,
                    OBJECTMAPPER.writeValueAsString(tenacityConfiguration));
        } catch (Exception err) {
            LOGGER.warn("Could not convert TenacityConfiguration to json", err);
            throw new RuntimeException(err);
        }
    }

    public DependencyEntity using(TenacityConfiguration configuration) {
        return DependencyEntity.build(getDependencyId(), getEnvironmentId(), configuration);
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

    public DependencyId getDependencyId() {
        return DependencyId.from(getPartitionKey());
    }

    public EnvironmentId getEnvironmentId() {
        return EnvironmentId.from(getRowKey());
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

    @Override
    public Class<? extends TableServiceEntity> getEntityClass() {
        return DependencyEntity.class;
    }

    @Override
    public AzureTableName getTable() {
        return TableId.DEPENDENCIES;
    }

    //** For Azure */
    @Deprecated
    public DependencyEntity() {
        super(TableId.DEPENDENCIES);
    }

    @Deprecated
    public String getTenacityConfigurationAsString() {
        return tenacityConfigurationAsString;
    }

    @Deprecated
    public void setTenacityConfigurationAsString(String tenacityConfigurationAsString) {
        this.tenacityConfigurationAsString = tenacityConfigurationAsString;
    }
}
