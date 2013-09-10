package com.yammer.breakerbox.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleDateParser {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleDateParser.class);

    public static long dateToMillis(String date) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(date).getTime();
        } catch (ParseException e) {
            LOGGER.warn("Unable to parse to millis from date {} : {}", date, e);
            return 0;
        }
    }

    public static String millisToDate(long timeMillis) {
        return millisToDate(String.valueOf(timeMillis));
    }

    public static String millisToDate(String timeMillis) {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date(Long.parseLong(timeMillis)));
    }
}
