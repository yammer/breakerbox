package com.yammer.breakerbox.service.comparable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.junit.Test;

import java.util.ArrayList;

import static com.yammer.breakerbox.service.comparable.DescendingRowOrderTest.StubTableType;
import static org.fest.assertions.api.Assertions.assertThat;

public class DescendingRowVersionFirstOrderTest {
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

        final ImmutableList<StubTableType> sortedItems = Ordering.from(new DescendingRowVersionFirstOrder<>("12"))
                .immutableSortedCopy(items);

        assertThat(sortedItems.size()).isEqualTo(7);
        assertThat(Long.valueOf(sortedItems.get(0).getRowKey())).isEqualTo(12); //12 gets priority
        assertThat(Long.valueOf(sortedItems.get(1).getRowKey())).isEqualTo(14);
        assertThat(Long.valueOf(sortedItems.get(2).getRowKey())).isEqualTo(13);
        assertThat(Long.valueOf(sortedItems.get(3).getRowKey())).isEqualTo(10);
        assertThat(Long.valueOf(sortedItems.get(4).getRowKey())).isEqualTo(8);
        assertThat(sortedItems.get(5).getRowKey())
                .isEqualTo(sortedItems.get(6).getRowKey())
                .isEqualToIgnoringCase("null");
    }

}
