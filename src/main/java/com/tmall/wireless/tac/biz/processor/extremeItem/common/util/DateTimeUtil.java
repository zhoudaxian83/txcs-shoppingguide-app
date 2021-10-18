package com.tmall.wireless.tac.biz.processor.extremeItem.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId zoneId = ZoneId.of("+8");

    /**
     * 将毫秒时间戳格式化成"YYYY-MM-dd HH:mm:ss"的格式
     *
     * @param timestamp 毫秒时间戳
     * @return "YYYY-MM-dd HH:mm:ss"格式的字符串
     */
    public static String formatTimestamp(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        return dateTimeFormatter.format(LocalDateTime.ofInstant(instant, zoneId));
    }

    public static void main(String[] args) {
        String s = DateTimeUtil.formatTimestamp(1636632111000L);
        System.out.println("s = " + s);
    }
}
