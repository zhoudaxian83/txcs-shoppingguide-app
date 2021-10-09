package com.tmall.wireless.tac.biz.processor.extremeItem.common.util;

import org.slf4j.LoggerFactory;

public class LoggerProxy {

    public static Logger getLogger(Class<?> clazz) {
        return new Logger(LoggerFactory.getLogger(clazz));
    }
}
