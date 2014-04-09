package com.yammer.breakerbox.azure.model;

import com.yammer.breakerbox.store.model.DependencyModel;
import com.yammer.breakerbox.store.model.ServiceModel;
import org.joda.time.DateTime;

public class Entities {
    private Entities() {}
    
    public static ServiceModel toModel(ServiceEntity serviceEntity) {
        return new ServiceModel(serviceEntity.getServiceId(), serviceEntity.getDependencyId());
    }
    
    public static DependencyModel toModel(DependencyEntity dependencyEntity) {
        return new DependencyModel(
                dependencyEntity.getDependencyId(),
                new DateTime(dependencyEntity.getConfigurationTimestamp()),
                dependencyEntity.getConfiguration().get(),
                dependencyEntity.getUser(),
                dependencyEntity.getServiceId());
    }
}
