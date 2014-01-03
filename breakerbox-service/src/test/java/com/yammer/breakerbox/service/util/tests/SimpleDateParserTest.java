package com.yammer.breakerbox.service.util.tests;

import com.yammer.breakerbox.service.util.SimpleDateParser;
import org.apache.commons.lang.time.DateFormatUtils;
import org.fest.assertions.api.Assertions;
import org.junit.Test;

import java.util.Date;

public class SimpleDateParserTest {

    private static final long testDateEpoch = 1345938944000l;
    private static final String testDateFormatted = "2012-08-25 16:55:44";

    @Test
    public void testToDate() throws Exception {
        Assertions.assertThat(SimpleDateParser.millisToDate(String.valueOf(testDateEpoch))).isEqualTo(DateFormatUtils.format(new Date(testDateEpoch), SimpleDateParser.DATE_FORMAT));
    }
}
