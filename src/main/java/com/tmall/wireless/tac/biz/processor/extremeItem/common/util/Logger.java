package com.tmall.wireless.tac.biz.processor.extremeItem.common.util;

import com.taobao.eagleeye.EagleEye;
import org.slf4j.LoggerFactory;

public class Logger {
    private static org.slf4j.Logger outerLogger = LoggerFactory.getLogger(Logger.class);

    private org.slf4j.Logger logger;

    public Logger(org.slf4j.Logger logger) {
        this.logger = logger;
    }

    public void debug(String msg) {
        if(isEagleEyeTest()) {
            return;
        }
        this.logger.debug(msg);
    }

    public void info(String msg) {
        if(isEagleEyeTest()) {
            return;
        }
        this.logger.info(msg);
    }

    public void warn(String msg) {
        if(isEagleEyeTest()) {
            return;
        }
        this.logger.warn(msg);
    }

    public void error(String msg) {
        this.logger.error(msg);
    }

    public void error(String msg, Throwable e) {
        this.logger.error(msg, e);
    }

    public static boolean isEagleEyeTest() {
        try {
            String ut = EagleEye.getUserData("t");
            if ("1".equals(ut) || "2".equals(ut)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            outerLogger.error("method isEagleEyeTest error.", e);
            return false;
        }
    }
}

