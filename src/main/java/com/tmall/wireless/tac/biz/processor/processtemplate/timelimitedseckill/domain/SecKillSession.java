package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain;

import com.tmall.wireless.tac.biz.processor.processtemplate.common.util.DateTimeUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
    private SecKillSessionStatus status;

    private SecKillSession() {}

    public LocalDateTime startTime() {
        return this.startTime;
    }

    public Long id() {
        return this.id;
    }

    public String sessionText() {
        return this.sessionText;
    }

    public Long startTimestamps() {
        return this.startTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }

    public Long endTimestamps() {
        return this.endTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }

    public String parseHHMMofStartTime() {
        return DateTimeUtil.formatHH_MM(this.startTime);
    }

    public String itemSetId() {
        return this.itemSetId;
    }

    public SecKillSessionStatus status() {
        return this.status;
    }

    public int statusVal() {
        return this.status.statusVal();
    }

    /**
     * 倒计时时间（毫秒）
     * 如果秒杀场次状态为"未开始"，则为当前时间和开始时间的差值
     * 如果秒杀场次状态为"秒杀中"，则为当前时间和结束时间的差值
     * 如果秒杀场次状态为"已结束"，则返回无效值-1
     *
     * @return 倒计时时间
     */
    public long countDownMillis() {
        if(status == SecKillSessionStatus.NOT_STARTED) {
            return Duration.between(LocalDateTime.now(), this.startTime).toMillis();
        } else if(status == SecKillSessionStatus.IN_PROCESS) {
            return Duration.between(LocalDateTime.now(), this.endTime).toMillis();
        } else {
            return -1L;
        }
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

        if(secKillSession.startTime.isAfter(secKillSession.endTime)) {
             return null;
        }

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
    private static SecKillSessionStatus initStatus(LocalDateTime baseCurrentTime, LocalDateTime startTime, LocalDateTime endTime) {
        if(startTime == null || endTime == null) {
            //正常不会发生，打底当作已结束处理
            return SecKillSessionStatus.OVER;
        }
        if(baseCurrentTime.isBefore(startTime)) {
            return SecKillSessionStatus.NOT_STARTED;
        } else if(baseCurrentTime.isBefore(endTime)) {
            return SecKillSessionStatus.IN_PROCESS;
        }
        return SecKillSessionStatus.OVER;
    }

    /**
     * 是否是有效的秒杀场次，已结束（2）的状态不是有效的秒杀场次，其余的未开始（2）和进行中（1）的为有效的秒杀场次
     *
     * @return true if status == 0 or status == 1 else return false.
     */
    public boolean isValid() {
        return this.status == SecKillSessionStatus.NOT_STARTED || this.status == SecKillSessionStatus.IN_PROCESS;
    }
}
