package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.context;

import java.io.Serializable;
import java.util.Calendar;

import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderTimeUtil;
import lombok.Getter;
import lombok.Setter;

public class TimeInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**日期，格式：2020-11-12*/
    @Getter @Setter
    private String dateString;

    /**小时，格式：9、12、23*/
    @Getter @Setter
    private String hour;

    /**预览模式，格式：*/
    @Getter @Setter
    private Long previewTime;

    /**获取系统当前日期，格式：2020-11-12*/
    private String getNowDateStr(){
        Calendar now = Calendar.getInstance();
        return RenderTimeUtil.dateFormatThreadLocal.get().format(now.getTime());
    }

    /**获取系统当前小时，格式：9、12、23*/
    private int getNowHour(){
        Calendar now = Calendar.getInstance();
        return now.get(Calendar.HOUR_OF_DAY);
    }
}
