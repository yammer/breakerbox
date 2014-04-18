package com.yammer.breakerbox.azure.model;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.yammer.breakerbox.store.model.DependencyModel;
import com.yammer.breakerbox.store.model.ServiceModel;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

public class Entities {
    private Entities() {}

    public static Optional<ServiceModel> toServiceModel(Optional<ServiceEntity> serviceEntity) {
        if (serviceEntity.isPresent()) {
            return Optional.of(Entities.toModel(serviceEntity.get()));
        }
        return Optional.absent();
    }
    
    public static ServiceModel toModel(ServiceEntity serviceEntity) {
        return new ServiceModel(serviceEntity.getServiceId(), serviceEntity.getDependencyId());
    }

    public static ServiceEntity from(ServiceModel serviceModel) {
        return ServiceEntity.build(serviceModel.getServiceId(), serviceModel.getDependencyId());
    }

    public static Optional<DependencyModel> toDependencyModel(Optional<DependencyEntity> dependencyEntity) {
        if (dependencyEntity.isPresent()) {
            return Optional.of(Entities.toModel(dependencyEntity.get()));
        }
        return Optional.absent();
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

    public static ImmutableList<ServiceModel> toServiceModelList(Iterable<ServiceEntity> serviceEntities) {
        return FluentIterable
                .from(serviceEntities)
                .transform(new Function<ServiceEntity, ServiceModel>() {
                    @Override
                    public ServiceModel apply(ServiceEntity input) {
                        return Entities.toModel(checkNotNull(input));
                    }
                })
                .toList();
    }

    public static ImmutableList<DependencyModel> toDependencyModelList(Iterable<DependencyEntity> dependencyEntities) {
        return FluentIterable
                .from(dependencyEntities)
                .transform(new Function<DependencyEntity, DependencyModel>() {
                    @Override
                    public DependencyModel apply(DependencyEntity input) {
                        return Entities.toModel(checkNotNull(input));
                    }
                })
                .toList();
    }
}
