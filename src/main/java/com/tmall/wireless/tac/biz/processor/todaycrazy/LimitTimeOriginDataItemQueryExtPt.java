package com.tmall.wireless.tac.biz.processor.todaycrazy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterDataSetItemRuleDTO;
import com.tmall.aselfmanager.client.columncenter.response.PmtRuleDataItemRuleDTO;
import com.tmall.dataenginer.client.api.crowd.domain.KvBasedFeature;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.todaycrazy.model.LimitBuyDto;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.AldInfoUtil;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.TairUtil;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author guijian
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY)
public class LimitTimeOriginDataItemQueryExtPt implements OriginDataItemQueryExtPt {
    Logger LOGGER = LoggerFactory.getLogger(LimitTimeBuyScene.class);

    @Autowired
    TairUtil tairUtil;
    @Autowired
    AldInfoUtil aldInfoUtil;
    @Autowired
    TacLogger tacLogger;

    private static final int NAME_SPACE = 184;

    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem sgFrameworkContextItem) {
        OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();

        Map<String, Object> params = sgFrameworkContextItem.getRequestParams();
        //第几个时间段
        int index = aldInfoUtil.getIndex(params);
        //ald排期信息
        Map<String,String> map = aldInfoUtil.getAldInfo(params);
        LinkedHashMap<Long,Long> linkedHashMap = aldInfoUtil.buildTime(map);
        List<LimitBuyDto> limitBuyDtos = Lists.newArrayList();
        //打标命中的时间段
        aldInfoUtil.buildNowTime(linkedHashMap,index,limitBuyDtos);
        LOGGER.info("****LimitTimeOriginDataItemQueryExtPt buildNowTime limitBuyDtos***"+limitBuyDtos);
        Long hitStartTime = 0L;
        Long hitEndTime = 0L;

        for(LimitBuyDto limitBuyDto:limitBuyDtos){
            if(limitBuyDto.getIsHit()){
                hitStartTime = limitBuyDto.getStartTime();
                hitEndTime = limitBuyDto.getEndTime();
            }
        }

        List<ColumnCenterDataSetItemRuleDTO> hitpmtRuleDataItemRuleDTOList = Lists.newArrayList();
        List<PmtRuleDataItemRuleDTO> pmtRuleDataItemRuleDTOList = tairUtil.getCacheData();
        LOGGER.info("****LimitTimeOriginDataItemQueryExtPt pmtRuleDataItemRuleDTOList.size()***"+pmtRuleDataItemRuleDTOList.size());
        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY)
            .kv("tair pmtRuleDataItemRuleDTOList.size()",String.valueOf(pmtRuleDataItemRuleDTOList.size()))
            .kv("tair pmtRuleDataItemRuleDTOList.size()",JSON.toJSONString(pmtRuleDataItemRuleDTOList))
            .info();
        for(PmtRuleDataItemRuleDTO pmtRule : pmtRuleDataItemRuleDTOList){
            List<ColumnCenterDataSetItemRuleDTO> itemList = pmtRule.getDataSetItemRuleDTOList();
            LOGGER.info("****LimitTimeOriginDataItemQueryExtPt itemList.size()***"+itemList.size());
            LOGGER.info("****LimitTimeOriginDataItemQueryExtPt JSON.toJSONString(itemList)***"+JSON.toJSONString(itemList));
            for(ColumnCenterDataSetItemRuleDTO item : itemList){
                Long startTime = item.getDataRule().getItemScheduleStartTime().getTime()/1000;
                Long endTime = item.getDataRule().getItemScheduleEndTime().getTime()/1000;
                if(startTime <= hitStartTime && endTime >= (hitEndTime-1)){
                    hitpmtRuleDataItemRuleDTOList.add(item);
                }
            }
        }
        LOGGER.info("****LimitTimeOriginDataItemQueryExtPt hitpmtRuleDataItemRuleDTOList.size()***"+hitpmtRuleDataItemRuleDTOList.size());
        originDataDTO.setResult(aldInfoUtil.buildItemList(dingKengDeal(hitpmtRuleDataItemRuleDTOList)));
        return Flowable.just(originDataDTO);
    }

    /**
     * 商品定坑
     * @param hitpmtRuleDataItemRuleDTOList
     * @return
     */
    public List<ColumnCenterDataSetItemRuleDTO> dingKengDeal(List<ColumnCenterDataSetItemRuleDTO> hitpmtRuleDataItemRuleDTOList){
        if(CollectionUtils.isEmpty(hitpmtRuleDataItemRuleDTOList)){
            return hitpmtRuleDataItemRuleDTOList;
        }
        List<ColumnCenterDataSetItemRuleDTO> dingKengColumnCenterDataSetItemRuleDTO = Lists.newArrayList();
        Map<Long,ColumnCenterDataSetItemRuleDTO> stickMap = new HashMap<>();
        List<ColumnCenterDataSetItemRuleDTO> originList = new ArrayList<>();

        hitpmtRuleDataItemRuleDTOList.forEach(item -> {
            Long stick = item.getDataRule().getStick();
            HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY)
                .kv("dingKengDeal","dingKengDeal")
                .kv("ItemId",String.valueOf(item.getItemId()))
                .kv("stick",String.valueOf(stick))
                .kv("item",JSON.toJSONString(item))
                .kv("stick != null && 1L <= stick && stick <= hitpmtRuleDataItemRuleDTOList.size()",JSON.toJSONString(stick != null && 1L <= stick && stick <= hitpmtRuleDataItemRuleDTOList.size()))
                .info();

            if(stick != null && 1L <= stick && stick <= hitpmtRuleDataItemRuleDTOList.size()){
                stickMap.put(stick,item);
            }else{
                originList.add(item);
            }
            HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY)
                .kv("dingKengDeal","dingKengDeal")
                .kv("stickMap",JSON.toJSONString(stickMap))
                .kv("originList",JSON.toJSONString(originList))
                .info();
        });
        int j = 0;
        for(int i=1;i<hitpmtRuleDataItemRuleDTOList.size();i++){
            if(stickMap.containsKey(Long.valueOf(i))){
                dingKengColumnCenterDataSetItemRuleDTO.add(stickMap.get(Long.valueOf(i)));
            }else{
                dingKengColumnCenterDataSetItemRuleDTO.add(originList.get(j));
                j++;
            }
        }
        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY)
            .kv("dingKengDeal","dingKengDeal")
            .kv("dingKengColumnCenterDataSetItemRuleDTO.size()",JSON.toJSONString(dingKengColumnCenterDataSetItemRuleDTO.size()))
            .kv("dingKengColumnCenterDataSetItemRuleDTO",JSON.toJSONString(dingKengColumnCenterDataSetItemRuleDTO))
            .info();
        return dingKengColumnCenterDataSetItemRuleDTO;
    }
}
