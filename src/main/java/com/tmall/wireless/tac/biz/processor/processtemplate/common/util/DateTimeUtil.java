package com.tmall.wireless.tac.biz.processor.processtemplate.common.util;

import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter hhMMFormatter = DateTimeFormatter.ofPattern("HH:mm");
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

    /**
     * 将"YYYY-MM-dd HH:mm:ss"字符串格式转化为LocalDateTime类型
     *
     * @param dateTimeStr "YYYY-MM-dd HH:mm:ss"格式的字符串
     * @return LocalDateTime对象
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        LocalDateTime result = null;
        if(StringUtils.isBlank(dateTimeStr)) {
            return null;
        }
        try {
            result = LocalDateTime.parse(dateTimeStr, dateTimeFormatter);
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    /**
     * 将LocalDateTime对象格式化成"HH:MM"的格式，即只截取小时和分钟部分，如"08:00"
     *
     * @param dateTime 将要格式华的LocalDateTime对象
     * @return "HH:MM"的格式的字符串
     */
    public static String formatHH_MM(LocalDateTime dateTime) {
        return hhMMFormatter.format(dateTime);
    }

    public static void main(String[] args) {
        String s = DateTimeUtil.formatTimestamp(1636632111000L);
        System.out.println("s = " + s);

        LocalDateTime localDateTime = parseDateTime("2021-11-11 20:01:51");
        System.out.println("localDateTime = " + localDateTime);

        String formatHH_mm = DateTimeUtil.formatHH_MM(LocalDateTime.now());
        System.out.println("formatHH_mm = " + formatHH_mm);
    }
}
