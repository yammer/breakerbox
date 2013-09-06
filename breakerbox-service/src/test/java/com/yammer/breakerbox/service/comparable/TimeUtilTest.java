package com.yammer.breakerbox.service.comparable;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class TimeUtilTest {
    @Test
    public void testTrimMillis() throws Exception {
        assertThat(TimeUtil.trimMillis("1")).isEqualTo("1");
        assertThat(TimeUtil.trimMillis("12")).isEqualTo("12");
        assertThat(TimeUtil.trimMillis("123")).isEqualTo("123");
        assertThat(TimeUtil.trimMillis("1234")).isEqualTo("1");
        assertThat(TimeUtil.trimMillis("12345")).isEqualTo("12");
    }
}
