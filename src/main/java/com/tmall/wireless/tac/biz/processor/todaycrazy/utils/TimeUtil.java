package com.tmall.wireless.tac.biz.processor.todaycrazy.utils;

import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
    /**
     * 获取某一天的时间戳
     * @param addDay
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    public static Long getEndTimestamp(Date date, int addDay, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, addDay);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        Date start = calendar.getTime();
        return start.getTime()/1000;
    }

    /**
     * 获取某一天日期
     * @param date
     * @param addDay
     * @return
     */
    public static Date getDate(Date date, int addDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, addDay);
        Date start = calendar.getTime();
        return start;
    }
}
