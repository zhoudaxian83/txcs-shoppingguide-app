package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class SelectedSecKillSession {
    private Long id;
    private String itemSetId;
    private int status;
    private LocalDateTime startTime;

    public SelectedSecKillSession(Long id, String itemSetId, int status, LocalDateTime startTime) {
        this.id = id;
        this.itemSetId = itemSetId;
        this.status = status;
        this.startTime = startTime;
    }

    public Long id() {
        return this.id;
    }

    public String itemSetId() {
        return itemSetId;
    }

    /**
     * 查询未来价格的时间，如果返回不为空，则查询未来时间的价格
     * 如果返回为空，则查询当前价格
     * 注意：开始时间特意往后偏移了59秒，避免优惠价在开始时间那一刻还没生效
     * 具体偏移的秒数还是需要具体情况具体分析，只要没有一分钟内的优惠，59秒是个合适的选择。
     *
     * @return 查询未来价格的时间
     */
    public Long timeOfFuturePrice() {
        if(status == 0 || status == 1) {
            return null;
        }
        return startTime.plusSeconds(59).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }
}
