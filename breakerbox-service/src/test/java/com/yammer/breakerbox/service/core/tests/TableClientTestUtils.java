package com.yammer.breakerbox.service.core.tests;

import com.google.common.base.Optional;
import com.yammer.breakerbox.azure.TableClient;
import com.yammer.breakerbox.azure.core.TableKey;
import com.yammer.breakerbox.azure.core.TableType;

import static org.junit.Assert.assertTrue;

public class TableClientTestUtils {
    public static <T extends TableKey> void tearDownTestTable(TableClient tableClient, T build) {
        final Optional<? extends TableType> entity = tableClient.retrieve(build);
        if (entity.isPresent()) {
            assertTrue(tableClient.remove(entity.get()));
        }
    }
}
