package com.yammer.azure.healthchecks.test;

import com.google.common.collect.ImmutableList;
import com.yammer.azure.TableClient;
import com.yammer.azure.healthchecks.TableClientHealthcheck;
import com.yammer.breakerbox.service.azure.TableId;
import com.yammer.metrics.core.HealthCheck;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TableClientHealthcheckTest {

    @Test
    public void healthy() throws Exception {
        final TableClient mockTableClient = mock(TableClient.class);
        final TableClientHealthcheck tableClientHealthcheck = new TableClientHealthcheck(mockTableClient);

        when(mockTableClient.listTables()).thenReturn(ImmutableList.of(TableId.SERVICES.toString()));
        assertThat(tableClientHealthcheck.check()).isEqualTo(HealthCheck.Result.healthy());
    }

    @Test
    public void unhealthy() throws Exception {
        final TableClient mockTableClient = mock(TableClient.class);
        final TableClientHealthcheck tableClientHealthcheck = new TableClientHealthcheck(mockTableClient);

        when(mockTableClient.listTables()).thenReturn(ImmutableList.<String>of());
        assertThat(tableClientHealthcheck.check()).isEqualTo(HealthCheck.Result.unhealthy("Could not list tables"));
    }
}