package com.yammer.breakerbox.service.util;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class SimpleDateParserTest {

    private static final long testDateEpoch = 1345938944000l;
    private static final String testDateFormatted = "2012-08-25 16:55:44";

    @Test
    public void testToMillis() throws Exception {
        assertThat(SimpleDateParser.dateToMillis(testDateFormatted)).isEqualTo(testDateEpoch);
        assertThat(SimpleDateParser.dateToMillis("2012-02-03 10:12:14 some more text on the end")).isEqualTo(1328292734000l);
        assertThat(SimpleDateParser.dateToMillis("2012-02-03 10:13:14 plus a minute")).isEqualTo(1328292794000l);
    }

    @Test
    public void testToDate() throws Exception {
        assertThat(SimpleDateParser.millisToDate(String.valueOf(testDateEpoch))).isEqualTo(testDateFormatted);
    }
}
