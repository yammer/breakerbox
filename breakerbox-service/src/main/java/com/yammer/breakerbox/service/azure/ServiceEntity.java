package com.yammer.breakerbox.service.azure;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Bean to represent the Service table.
 * Partitioned by service, each service contains a list of service dependency keys.
 * As of 5-SEP-2013, these dependency keys are updated by the continuous polling of services'
 * Tenacity endpoint for DependencyKeys. See TenacityClient for more.
 */
public class ServiceEntity extends TableType implements TableKey {
    private String tenacityConfigurationAsString;
    private static final ObjectMapper OBJECTMAPPER = new ObjectMapperFactory().build();
    private static final Validator VALIDATOR = new Validator();
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEntity.class);

    private ServiceEntity(ServiceId serviceId,
                          DependencyId dependencyId,
                          String tenacityConfigurationAsString) {
        super(TableId.SERVICE);
        this.partitionKey = serviceId.getId();
        this.rowKey = dependencyId.getId();
        this.tenacityConfigurationAsString = tenacityConfigurationAsString;
    }

    public ServiceId getServiceId() {
        return ServiceId.from(getPartitionKey());
    }

    public DependencyId getDependencyId() {
        return DependencyId.from(getRowKey());
    }

    public static ServiceEntity build(ServiceId serviceId,
                                      DependencyId dependencyId) {
        return build(serviceId, dependencyId, new TenacityConfiguration());
    }

    public static ServiceEntity build(ServiceId serviceId,
                                      DependencyId dependencyId,
                                      TenacityConfiguration tenacityConfiguration) {
        try {
            return new ServiceEntity(
                    serviceId,
                    dependencyId,
                    OBJECTMAPPER.writeValueAsString(tenacityConfiguration)); //TODO: pshaw 08-30 this line & try/catch will go away
        } catch (Exception err) {
            LOGGER.warn("Could not convert TenacityConfiguration to json", err);
            throw new RuntimeException(err);
        }
    }

    public ServiceEntity using(TenacityConfiguration configuration) {
        return ServiceEntity.build(getServiceId(), getDependencyId(), configuration);
    }

    //TODO pshaw 08-30 this has to die
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ServiceEntity entity = (ServiceEntity) o;

        if (tenacityConfigurationAsString != null ? !tenacityConfigurationAsString.equals(entity.tenacityConfigurationAsString) : entity.tenacityConfigurationAsString != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (tenacityConfigurationAsString != null ? tenacityConfigurationAsString.hashCode() : 0);
        return result;
    }

    @Override
    public Class<? extends TableServiceEntity> getEntityClass() {
        return ServiceEntity.class;
    }

    @Override
    public AzureTableName getTable() {
        return TableId.SERVICE;
    }

    /** For Azure */

    @Deprecated
    public ServiceEntity() {
        super(TableId.SERVICE);
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
