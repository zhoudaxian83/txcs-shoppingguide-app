package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain;

import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.DateTimeUtil;

import java.time.LocalDateTime;

public class SecKillSession {
    /**
     * 秒杀场次ID
     */
    private Long id;

    /**
     * 秒杀场次文案描述，如" 母婴专场"
     */
    private String sessionText;

    /**
     * 秒杀场次开始时间
     */
    private LocalDateTime startTime;

    /**
     * 秒杀场次结束时间
     */
    private LocalDateTime endTime;

    /**
     * 圈品集ID
     */
    private String itemSetId;

    /**
     * 秒杀场次状态，0-未开始，1-秒杀中，2-已结束
     */
    private int status;

    private SecKillSession() {}

    public LocalDateTime startTime() {
        return this.startTime;
    }

    public Long id() {
        return this.id;
    }

    public String itemSetId() {
        return this.itemSetId;
    }

    /**
     * 根据基准时间和配置信息初始化秒杀场次，多个秒杀场次需要用同一个基准时间
     *
     * @param baseCurrentTime 基准当前时间
     * @param sessionConfig 秒杀场次配置
     * @return 秒杀场次
     */
    public static SecKillSession init(LocalDateTime baseCurrentTime, SecKillSessionConfig sessionConfig) {
        if(sessionConfig == null) {
            return null;
        }
        SecKillSession secKillSession = new SecKillSession();
        secKillSession.id = sessionConfig.getId();
        secKillSession.sessionText = sessionConfig.getSessionText();
        secKillSession.startTime = DateTimeUtil.parseDateTime(sessionConfig.getStartTime());
        secKillSession.endTime = DateTimeUtil.parseDateTime(sessionConfig.getEndTime());
        secKillSession.itemSetId = sessionConfig.getItemSetId();
        secKillSession.status = initStatus(baseCurrentTime, secKillSession.startTime, secKillSession.endTime);

        return secKillSession;
    }

    /**
     * 初始化秒杀场次的状态
     * 1、如果当前时间在开始时间之前，则为0-未开始
     * 2、如果当前时间在开始时间之后，在结束时间之前，则为1-秒杀中
     * 3、如果当前时间在结束时间之后，则为2-已结束
     *
     * @param baseCurrentTime 基准当前时间，一个秒杀活动中的多个秒杀场次均需要用同一个基准当前时间进行状态的初始化
     * @param startTime 秒杀场次开始时间
     * @param endTime 秒杀场次结束时间
     * @return 秒杀场次的状态
     */
    private static int initStatus(LocalDateTime baseCurrentTime, LocalDateTime startTime, LocalDateTime endTime) {
        if(startTime == null || endTime == null) {
            //正常不会发生，打底当作已结束处理
            return 2;
        }
        if(baseCurrentTime.isBefore(startTime)) {
            return 0;
        } else if(baseCurrentTime.isBefore(endTime)) {
            return 1;
        }
        return 2;
    }

    /**
     * 是否是有效的秒杀场次，已结束（2）的状态不是有效的秒杀场次，其余的未开始（2）和进行中（1）的为有效的秒杀场次
     *
     * @return true if status == 0 or status == 1 else return false.
     */
    public boolean isValid() {
        return this.status == 0 || this.status == 1;
    }
}
