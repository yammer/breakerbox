package com.yammer.avalanche.service.azure;


import com.yammer.azure.core.TableType;
import com.yammer.tenacity.core.config.TenacityConfiguration;

public class TenacityEntity extends TableType {
    private final TenacityConfiguration tenacityConfiguration;

    public TenacityEntity(TenacityConfiguration tenacityConfiguration) {
        super(TableId.TENACITY_SERVICE);
        this.tenacityConfiguration = tenacityConfiguration;
    }

    public TenacityConfiguration getTenacityConfiguration() {
        return tenacityConfiguration;
    }
}
