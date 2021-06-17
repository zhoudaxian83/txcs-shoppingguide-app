package com.tmall.wireless.tac.biz.processor.todaycrazy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterDataSetItemRuleDTO;
import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterPmtRuleDataSetDTO;
import com.tmall.aselfmanager.client.columncenter.response.PmtRuleDataItemRuleDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.todaycrazy.model.LimitBuyDto;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.AldInfoUtil;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.MapUtil;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.TairUtil;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.TodayCrazyUtils;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import io.reactivex.Flowable;
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
        for(PmtRuleDataItemRuleDTO pmtRule : pmtRuleDataItemRuleDTOList){
            List<ColumnCenterDataSetItemRuleDTO> itemList = pmtRule.getDataSetItemRuleDTOList();
            LOGGER.info("****LimitTimeOriginDataItemQueryExtPt itemList.size()***"+itemList.size());
            LOGGER.info("****LimitTimeOriginDataItemQueryExtPt JSON.toJSONString(itemList)***"+JSON.toJSONString(itemList));
            setChannelKey(sgFrameworkContextItem,pmtRule);
            for(ColumnCenterDataSetItemRuleDTO item : itemList){
                Long startTime = item.getDataRule().getItemScheduleStartTime().getTime()/1000;
                Long endTime = item.getDataRule().getItemScheduleEndTime().getTime()/1000;
                if(startTime <= hitStartTime && endTime >= (hitEndTime-1)){
                    hitpmtRuleDataItemRuleDTOList.add(item);
                }
            }
        }
        LOGGER.info("****LimitTimeOriginDataItemQueryExtPt hitpmtRuleDataItemRuleDTOList.size()***"+hitpmtRuleDataItemRuleDTOList.size());
        originDataDTO.setResult(aldInfoUtil.buildItemList(hitpmtRuleDataItemRuleDTOList));
        return Flowable.just(originDataDTO);
    }

    }
}
