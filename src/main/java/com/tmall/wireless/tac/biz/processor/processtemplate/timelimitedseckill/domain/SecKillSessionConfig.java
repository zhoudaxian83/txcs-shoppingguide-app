package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀场次配置
 */
@Data
public class SecKillSessionConfig {
    /**
     * 秒杀场次ID，与鸿雁数据集的contentId对应
     */
    private Long id;

    /**
     * 秒杀场次文案描述，如" 母婴专场"，透传给前端前，前端逻辑：
     * 1、对于未开始的场次，如果该字段不为空，则使用该字段，为空则使用“即将开始”
     * 2、对于已开始的场次，无论配置与否，均展示“秒杀中”
     */
    private String sessionText;

    /**
     * 秒杀场次开始时间
     */
    private String startTime;

    /**
     * 秒杀场次结束时间
     */
    private String endTime;

    /**
     * 圈品集ID
     */
    private String itemSetId;
}
