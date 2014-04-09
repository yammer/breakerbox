package com.yammer.breakerbox.azure.tests;

import com.google.common.base.Optional;
import com.yammer.breakerbox.azure.TableClient;
import com.yammer.breakerbox.azure.core.TableKey;
import com.yammer.breakerbox.azure.core.TableType;

import static org.junit.Assert.assertTrue;

public class TableClientTester {
    private final TableClient tableClient;

    public TableClientTester(TableClient tableClient) {
        this.tableClient = tableClient;
    }

    public <T extends TableType> void remove(TableKey tableKey) {
        final Optional<T> result = tableClient.retrieve(tableKey);
        if (result.isPresent()) {
            assertTrue(tableClient.remove(result.get()));
        }
    }
}
