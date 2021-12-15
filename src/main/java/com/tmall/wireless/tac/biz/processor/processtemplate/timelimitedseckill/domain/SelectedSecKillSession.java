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
     *
     * @return 查询未来价格的时间
     */
    public Long timeOfFuturePrice() {
        if(status == 0 || status == 1) {
            return null;
        }
        return startTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }
}
