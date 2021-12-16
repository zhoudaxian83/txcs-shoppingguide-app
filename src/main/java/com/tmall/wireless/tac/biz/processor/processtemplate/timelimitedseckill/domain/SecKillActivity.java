package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain;

import org.apache.commons.collections.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SecKillActivity {
    private List<SecKillSession> secKillSessions;

    private List<SecKillSession> validSecKillSessions;

    /**
     * 根据配置初始化秒杀活动
     *
     * @param activityConfig
     * @return
     */
    public static SecKillActivity init(SecKillActivityConfig activityConfig) {
        SecKillActivity secKillActivity = new SecKillActivity();
        secKillActivity.secKillSessions = new ArrayList<>();
        List<SecKillSessionConfig> secKillSessionConfigs = activityConfig.getSecKillSessionConfigs();
        if(CollectionUtils.isEmpty(secKillSessionConfigs)) {
            return secKillActivity;
        }
        LocalDateTime baseCurrentTime = LocalDateTime.now();
        List<SecKillSession> tmpSecKillSessions = new ArrayList<>();
        for (SecKillSessionConfig secKillSessionConfig : secKillSessionConfigs) {
            SecKillSession secKillSession = SecKillSession.init(baseCurrentTime, secKillSessionConfig);
            if(secKillSession != null) {
                tmpSecKillSessions.add(secKillSession);
            }
        }
        //按照开始时间从早到晚排序
        List<SecKillSession> orderedSecKillSessions = tmpSecKillSessions.stream().sorted(Comparator.comparing(SecKillSession::startTime)).collect(Collectors.toList());
        secKillActivity.secKillSessions.addAll(orderedSecKillSessions);
        secKillActivity.validSecKillSessions = secKillActivity.secKillSessions.stream()
                .filter(SecKillSession::isValid).collect(Collectors.toList());
        return secKillActivity;
    }

    /**
     * 选中某个秒杀场次
     * 情况1：chooseId不为空
     * 则在有效场次中选择chooseId指定的秒杀场次，如果没有，则在有效场次中选择第一个有效场次，如果没有有效场次，则返回空
     * 情况2：choose为空
     * 则在有效场次中选择第一个有效场次，如果没有有效场次，则返回空
     * 有效场次的概念为"未结束"的秒杀场次，即"未开始"或者"进行中"
     *
     * @param chooseId 用户指定的秒杀场次ID
     * @return 某一个有效场次，如果没有有效的场次，则返回空
     */
    public SelectedSecKillSession select(Long chooseId) {
        SelectedSecKillSession result = null;
        if(chooseId != null) {
            result = this.validSecKillSessions.stream()
                    .filter(session -> chooseId.equals(session.id()))
                    .findFirst()
                    .map(session -> new SelectedSecKillSession(session.id(), session.itemSetId(), session.status(), session.startTime()))
                    .orElse(null);
        }
        if(result == null) {
            result = this.validSecKillSessions.stream()
                    .findFirst()
                    .map(session -> new SelectedSecKillSession(session.id(), session.itemSetId(), session.status(), session.startTime()))
                    .orElse(null);
        }

        return result;
    }

    public List<SecKillSession> validSecKillSessions() {
        return this.validSecKillSessions;
    }
}
