package com.tmall.wireless.tac.biz.processor.firstScreenMind.utils;

import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;

@Component
public class RenderTimeUtil {

    public static ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

}
