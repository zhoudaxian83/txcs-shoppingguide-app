package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain;

import lombok.Data;

import java.util.Map;

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

    public static SecKillSessionConfig valueOf(Map<String, Object> stringObjectMap) {
        SecKillSessionConfig sessionConfig = new SecKillSessionConfig();
        if(stringObjectMap.get("default_contentId") != null) {
            sessionConfig.setId(Long.valueOf(String.valueOf(stringObjectMap.get("default_contentId"))));
        }
        if(stringObjectMap.get("contentId") != null) {
            sessionConfig.setId(Long.valueOf(String.valueOf(stringObjectMap.get("contentId"))));
        }
        if(stringObjectMap.get("sessionText") != null) {
            sessionConfig.setSessionText(String.valueOf(stringObjectMap.get("sessionConfig")));
        }
        if(stringObjectMap.get("startTime") != null) {
            sessionConfig.setStartTime(String.valueOf(stringObjectMap.get("startTime")));
        }
        if(stringObjectMap.get("endTime") != null) {
            sessionConfig.setEndTime(String.valueOf(stringObjectMap.get("endTime")));
        }
        if(stringObjectMap.get("itemSetId") != null) {
            sessionConfig.setItemSetId(String.valueOf(stringObjectMap.get("itemSetId")));
        }
        return sessionConfig;
    }
}
