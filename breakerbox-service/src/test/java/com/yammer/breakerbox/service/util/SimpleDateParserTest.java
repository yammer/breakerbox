package com.yammer.breakerbox.service.util;

import org.apache.commons.lang.time.DateFormatUtils;
import org.junit.Test;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;

public class SimpleDateParserTest {

    private static final long testDateEpoch = 1345938944000l;
    private static final String testDateFormatted = "2012-08-25 16:55:44";

    @Test
    public void testToDate() throws Exception {
        assertThat(SimpleDateParser.millisToDate(String.valueOf(testDateEpoch))).isEqualTo(DateFormatUtils.format(new Date(testDateEpoch), SimpleDateParser.DATE_FORMAT));
    }
}
