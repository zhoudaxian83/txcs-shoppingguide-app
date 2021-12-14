package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain;

import com.google.common.collect.Lists;
import com.taobao.eagleeye.EagleEye;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.Logger;
import com.tmall.wireless.tac.biz.processor.extremeItem.common.util.LoggerProxy;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfig;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigs;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * 秒杀活动配置
 * 一个秒杀活动一般需要配置多个秒杀场次
 */
@Data
public class SecKillActivityConfig {

    private static Logger logger = LoggerProxy.getLogger(ItemConfigs.class);
    /**
     * 秒杀场次配置
     */
    List<SecKillSessionConfig> secKillSessionConfigs = Lists.newArrayList();

    public static SecKillActivityConfig valueOf(List<Map<String, Object>> aldDataList) {
        if(CollectionUtils.isEmpty(aldDataList)) {
            logger.error("SecKillActivityConfig.valueOf error,aldDataList empty, traceId:{}", EagleEye.getTraceId());
            throw new RuntimeException("秒杀场次配置数据不允许为空");
        }
        SecKillActivityConfig activityConfig = new SecKillActivityConfig();
        for (Map<String, Object> stringObjectMap : aldDataList) {
            SecKillSessionConfig sessionConfig = SecKillSessionConfig.valueOf(stringObjectMap);
            activityConfig.secKillSessionConfigs.add(sessionConfig);
        }
        activityConfig.check();
        return activityConfig;
    }

    private void check() {

    }
}
