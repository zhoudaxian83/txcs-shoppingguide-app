package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain;

public class SelectedSecKillSession {
    private Long id;
    private String itemSetId;

    public SelectedSecKillSession(Long id, String itemSetId) {
        this.id = id;
        this.itemSetId = itemSetId;
    }

    public Long id() {
        return this.id;
    }

    public String itemSetId() {
        return itemSetId;
    }
}
