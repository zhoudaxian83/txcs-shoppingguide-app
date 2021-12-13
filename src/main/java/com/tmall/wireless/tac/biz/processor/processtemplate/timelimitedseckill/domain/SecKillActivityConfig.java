package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain;

import lombok.Data;

import java.util.List;

/**
 * 秒杀活动配置
 * 一个秒杀活动一般需要配置多个秒杀场次
 */
@Data
public class SecKillActivityConfig {
    /**
     * 秒杀场次配置
     */
    List<SecKillSessionConfig> secKillSessionConfigs;
}
