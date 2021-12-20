package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain;

/**
 * 秒杀场次状态
 */
public enum SecKillSessionStatus {
    NOT_STARTED(0, "未开始"),
    IN_PROCESS(1, "进行中"),
    OVER(2, "已结束");

    private int status;
    private String desc;

    SecKillSessionStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public SecKillSessionStatus valueOf(int status) {
        if(status == 0) {
            return NOT_STARTED;
        } else if(status == 1) {
            return IN_PROCESS;
        } else if(status == 2) {
            return OVER;
        } else {
            throw new RuntimeException("不合法的秒杀状态, status:" + status);
        }
    }

    public int statusVal() {
        return this.status;
    }
}
