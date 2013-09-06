package com.yammer.breakerbox.service.comparable;

public class TimeUtil {
    public static final String LATEST = "LATEST";

    public static String trimMillis(String rowKey) {
        if(rowKey.length() < 4) return rowKey;
        return rowKey.substring(0, rowKey.length()-3);
    }
}
