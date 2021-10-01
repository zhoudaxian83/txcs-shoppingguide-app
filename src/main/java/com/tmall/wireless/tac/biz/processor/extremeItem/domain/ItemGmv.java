package com.tmall.wireless.tac.biz.processor.extremeItem.domain;

import lombok.Data;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

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
        if(lastNDaysGmv == null) {
            return 0;
        }
        return Arrays.stream(lastNDaysGmv).filter(Objects::nonNull).mapToDouble(d -> d).sum();
    }

    public double last1HourGmv() {
        if(this.last1HourGmv == null) {
            return 0;
        }
        return this.last1HourGmv;
    }


}
