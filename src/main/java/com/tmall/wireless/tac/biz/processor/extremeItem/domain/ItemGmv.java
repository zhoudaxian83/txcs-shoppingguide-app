package com.tmall.wireless.tac.biz.processor.extremeItem.domain;

import lombok.Data;

import java.util.Arrays;

@Data
public class ItemGmv {
    private Long itemId;
    /**
     * 前7天GMV
     */
    private Double[] last7DaysGmv;
    /**
     * 当天0点到当前时刻GMV
     */
    private Double todayGmv;
    /**
     * 前1个小时的GMV
     */
    private Double last1HourGmv;

    public double raceValue() {
        double last7DaysGmvSum = Arrays.stream(last7DaysGmv).mapToDouble(d -> d).sum();
        return last7DaysGmvSum + 0.5 * todayGmv + 0.5 * last1HourGmv;
    }
}
