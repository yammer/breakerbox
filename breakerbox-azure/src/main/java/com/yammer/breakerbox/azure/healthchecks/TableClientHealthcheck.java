package com.yammer.breakerbox.azure.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.Iterables;
import com.yammer.breakerbox.azure.TableClient;

import static com.google.common.base.Preconditions.checkNotNull;

public class TableClientHealthcheck extends HealthCheck {
    private final TableClient tableClient;

    public TableClientHealthcheck(TableClient tableClient) {
        this.tableClient = checkNotNull(tableClient, "tableClient cannot be null");
    }

    @Override
    public Result check() throws Exception {
        if (Iterables.isEmpty(tableClient.listTables())) {
            return Result.unhealthy("Could not list tables");
        } else {
            return Result.healthy();
        }
    }
}