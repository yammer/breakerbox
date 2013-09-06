package com.yammer.breakerbox.service.comparable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.yammer.azure.core.TableType;
import com.yammer.breakerbox.service.azure.TableId;
import org.junit.Test;

import java.util.ArrayList;
import static org.fest.assertions.api.Assertions.assertThat;


public class DescendingRowOrderTest {
    @Test
    public void testOrdering() throws Exception {
        final ArrayList<StubTableType> items = Lists.newArrayList(
                new StubTableType(10),
                new StubTableType(null),
                new StubTableType(8),
                new StubTableType(12),
                new StubTableType(14),
                new StubTableType(13),
                new StubTableType(null));

        final ImmutableList<StubTableType> sortedItems = Ordering.from(new DescendingRowOrder<StubTableType>())
                .immutableSortedCopy(items);

        assertThat(sortedItems.size()).isEqualTo(7);
        assertThat(Long.valueOf(sortedItems.get(0).getRowKey())).isEqualTo(14);
        assertThat(Long.valueOf(sortedItems.get(1).getRowKey())).isEqualTo(13);
        assertThat(Long.valueOf(sortedItems.get(2).getRowKey())).isEqualTo(12);
        assertThat(Long.valueOf(sortedItems.get(3).getRowKey())).isEqualTo(10);
        assertThat(Long.valueOf(sortedItems.get(4).getRowKey())).isEqualTo(8);
        assertThat(sortedItems.get(5).getRowKey())
                .isEqualTo(sortedItems.get(6).getRowKey())
                .isEqualToIgnoringCase("null");

    }

    private static class StubTableType extends TableType {
        protected StubTableType(Integer row) {
            super(TableId.SERVICE);
            this.rowKey = String.valueOf(row);
        }
    }

}
