package com.yammer.breakerbox.service.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleDateParser {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String millisToDate(String timeMillis) {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date(Long.parseLong(timeMillis)));
    }
}