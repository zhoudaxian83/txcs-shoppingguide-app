package com.tmall.wireless.tac.biz.processor.processtemplate.common.util;

import com.taobao.eagleeye.EagleEye;
import com.tmall.aselfcaptain.util.StackTraceUtil;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.Logger;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;

public class MetricsUtil {

    public static void mainProcessException(ProcessTemplateContext context, Exception e) {
        String sourceClassName = context.getSourceClassSimpleName();
        HadesLogUtil.stream(sourceClassName + "|mainProcess|" + Logger.isEagleEyeTest() + "|exception")
                .kv("errorMsg", StackTraceUtil.stackTrace(e))
                .kv("curPageUrl", context.getCurrentPageUrl())
                .kv("resourceId", context.getCurrentResourceId())
                .kv("scheduleId", context.getCurrentScheduleId())
                .kv("traceId", EagleEye.getTraceId())
                .error();
    }

    public static void mainProcessSuccess(ProcessTemplateContext context, long mainProcessStart) {
        String sourceClassName = context.getSourceClassSimpleName();
        long mainProcessEnd = System.currentTimeMillis();
        HadesLogUtil.stream(sourceClassName + "|mainProcess|" + Logger.isEagleEyeTest() + "|success|" + (mainProcessEnd - mainProcessStart))
                .info();
    }

    public static void contentExceeded(ProcessTemplateContext context, Integer expected, Integer actual) {
        String sourceClassName = context.getSourceClassSimpleName();
        HadesLogUtil.stream(sourceClassName + "|contentExceeded|" + Logger.isEagleEyeTest() + "|error")
                .kv("expected", String.valueOf(expected))
                .kv("actual", String.valueOf(actual))
                .kv("curPageUrl", context.getCurrentPageUrl())
                .kv("resourceId", context.getCurrentResourceId())
                .kv("scheduleId", context.getCurrentScheduleId())
                .kv("traceId", EagleEye.getTraceId())
                .error();
    }

    public static void itemExceeded(ProcessTemplateContext context, Integer expected, Integer actual) {
        String sourceClassName = context.getSourceClassSimpleName();
        HadesLogUtil.stream(sourceClassName + "|itemExceeded|" + Logger.isEagleEyeTest() + "|error")
                .kv("expected", String.valueOf(expected))
                .kv("actual", String.valueOf(actual))
                .kv("curPageUrl", context.getCurrentPageUrl())
                .kv("resourceId", context.getCurrentResourceId())
                .kv("scheduleId", context.getCurrentScheduleId())
                .kv("traceId", EagleEye.getTraceId())
                .error();
    }

    public static void recommendException(ProcessTemplateContext context, Exception e) {
        String sourceClassName = context.getSourceClassSimpleName();
        HadesLogUtil.stream(sourceClassName + "|recommend|" + Logger.isEagleEyeTest() + "|exception")
                .kv("errorMsg", StackTraceUtil.stackTrace(e))
                .kv("curPageUrl", context.getCurrentPageUrl())
                .kv("resourceId", context.getCurrentResourceId())
                .kv("scheduleId", context.getCurrentScheduleId())
                .kv("traceId", EagleEye.getTraceId())
                .error();
    }

    public static void recommendFail(ProcessTemplateContext context, String errorMsg) {
        String sourceClassName = context.getSourceClassSimpleName();
        HadesLogUtil.stream(sourceClassName + "|recommend|" + Logger.isEagleEyeTest() + "|fail")
                .kv("errorMsg", errorMsg)
                .kv("curPageUrl", context.getCurrentPageUrl())
                .kv("resourceId", context.getCurrentResourceId())
                .kv("scheduleId", context.getCurrentScheduleId())
                .kv("traceId", EagleEye.getTraceId())
                .error();
    }

    public static void recommendSuccess(ProcessTemplateContext context, long mainProcessStart) {
        String sourceClassName = context.getSourceClassSimpleName();
        long mainProcessEnd = System.currentTimeMillis();
        HadesLogUtil.stream(sourceClassName + "|recommend|" + Logger.isEagleEyeTest() + "|success|" + (mainProcessEnd - mainProcessStart))
                .info();
    }
}
