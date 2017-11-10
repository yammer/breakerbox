package com.yammer.breakerbox.azure.model;

import com.google.common.collect.ImmutableList;
import com.yammer.breakerbox.store.model.DependencyModel;
import com.yammer.breakerbox.store.model.ServiceModel;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Optional;

public class Entities {
    private Entities() {}

    public static Optional<ServiceModel> toServiceModel(Optional<ServiceEntity> serviceEntity) {
        return serviceEntity.map(Entities::toModel);
    }
    
    public static ServiceModel toModel(ServiceEntity serviceEntity) {
        return new ServiceModel(serviceEntity.getServiceId(), serviceEntity.getDependencyId());
    }

    public static ServiceEntity from(ServiceModel serviceModel) {
        return ServiceEntity.build(serviceModel.getServiceId(), serviceModel.getDependencyId());
    }

    public static Optional<DependencyModel> toDependencyModel(Optional<DependencyEntity> dependencyEntity) {
        return dependencyEntity.map(Entities::toModel);
    }
    
    public static DependencyModel toModel(DependencyEntity dependencyEntity) {
        return new DependencyModel(
                dependencyEntity.getDependencyId(),
                new DateTime(dependencyEntity.getConfigurationTimestamp()),
                dependencyEntity.getConfiguration().get(),
                dependencyEntity.getUser(),
                dependencyEntity.getServiceId());
    }

    public static DependencyEntity from(DependencyModel dependencyModel) {
        return DependencyEntity.build(
                dependencyModel.getDependencyId(),
                dependencyModel.getDateTime().getMillis(),
                dependencyModel.getUser(),
                dependencyModel.getTenacityConfiguration(),
                dependencyModel.getServiceId());
    }

    public static Collection<ServiceModel> toServiceModelList(Collection<ServiceEntity> serviceEntities) {
        return serviceEntities
                .stream()
                .map(Entities::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    public static Collection<DependencyModel> toDependencyModelList(Collection<DependencyEntity> dependencyEntities) {
        return dependencyEntities
                .stream()
                .map(Entities::toModel)
                .collect(ImmutableList.toImmutableList());
    }
}
