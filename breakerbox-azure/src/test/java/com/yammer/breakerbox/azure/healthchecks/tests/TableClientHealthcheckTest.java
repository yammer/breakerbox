package com.yammer.breakerbox.azure.healthchecks.tests;

import com.google.common.collect.ImmutableList;
import com.yammer.breakerbox.azure.TableClient;
import com.yammer.breakerbox.azure.core.TableId;
import com.yammer.breakerbox.azure.healthchecks.TableClientHealthcheck;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TableClientHealthcheckTest {

    @Test
    public void healthy() throws Exception {
        final TableClient mockTableClient = mock(TableClient.class);
        final TableClientHealthcheck tableClientHealthcheck = new TableClientHealthcheck(mockTableClient);

        when(mockTableClient.listTables()).thenReturn(ImmutableList.of(TableId.SERVICE.toString()));
        assertThat(tableClientHealthcheck.check().isHealthy()).isTrue();
    }

    @Test
    public void unhealthy() throws Exception {
        final TableClient mockTableClient = mock(TableClient.class);
        final TableClientHealthcheck tableClientHealthcheck = new TableClientHealthcheck(mockTableClient);

        when(mockTableClient.listTables()).thenReturn(ImmutableList.of());
        assertThat(tableClientHealthcheck.check().isHealthy()).isFalse();
    }
}