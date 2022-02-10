package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class SelectedSecKillSession {
    private Long id;
    private String itemSetId;
    private SecKillSessionStatus status;
    private LocalDateTime startTime;
    private Long fixPitItemId;

    public SelectedSecKillSession(Long id, String itemSetId, SecKillSessionStatus status, LocalDateTime startTime, Long fixPitItemId) {
        this.id = id;
        this.itemSetId = itemSetId;
        this.status = status;
        this.startTime = startTime;
        this.fixPitItemId = fixPitItemId;
    }

    public Long id() {
        return this.id;
    }

    public String itemSetId() {
        return itemSetId;
    }

    public Long fixPitItemId() {
        return this.fixPitItemId;
    }

    /**
     * 未开始的秒杀场次需要传递未来的时间去查询价格，已开始和已结束的秒杀场次返回null查询当前时间的价格
     *
     * @return 查询未来价格的时间
     */
    public Long timeOfFuturePrice() {
        if(this.status == SecKillSessionStatus.NOT_STARTED) {
            return startTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
        }
        return null;
    }
}
