package com.tmall.wireless.tac.biz.processor.extremeItem.domain;

import lombok.Data;

import java.util.Arrays;

@Data
public class ItemGmv {
    private Long itemId;
    /**
     * 前N天GMV,包含当天到当前时间
     */
    private Double[] lastNDaysGmv;
    /**
     * 前1个小时的GMV
     */
    private Double last1HourGmv;

    public double lastNDaysGmvSum() {
        return Arrays.stream(lastNDaysGmv).mapToDouble(d -> d).sum();
    }

    public double last1HourGmv() {
        return this.last1HourGmv;
    }
}
