package com.tmall.wireless.tac.biz.processor.todaycrazy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.alibaba.cola.extension.Extension;
import com.google.common.collect.Lists;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterDataSetItemRuleDTO;
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
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author guijian
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY)
public class LimitTimeOriginDataItemQueryExtPt implements OriginDataItemQueryExtPt {
    Logger LOGGER = LoggerFactory.getLogger(LimitTimeBuyScene.class);

    @Autowired
    TairFactorySpi tairFactorySpi;
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
        int index = MapUtil.getIntWithDefault(params,"index",0);
        LOGGER.info("****LimitTimeOriginDataItemQueryExtPt index***"+index);
        LOGGER.info("****LimitTimeOriginDataItemQueryExtPt aldInfoUtil***"+aldInfoUtil);
        LOGGER.info("****LimitTimeOriginDataItemQueryExtPt aldInfoUtil***"+aldInfoUtil);
        //ald排期信息
        Map<String,String> map = aldInfoUtil.getAldInfo(params);
        LinkedHashMap<Long,Long> linkedHashMap = aldInfoUtil.buildTime(map);
        tacLogger.info("****LimitTimeOriginDataItemQueryExtPt map***"+map);
        LOGGER.info("****LimitTimeOriginDataItemQueryExtPt map***"+map);
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
        List<PmtRuleDataItemRuleDTO> pmtRuleDataItemRuleDTOList = getCacheData();
        LOGGER.info("****LimitTimeOriginDataItemQueryExtPt pmtRuleDataItemRuleDTOList.size()***"+pmtRuleDataItemRuleDTOList.size());
        LOGGER.info("****LimitTimeOriginDataItemQueryExtPt pmtRuleDataItemRuleDTOList***"+pmtRuleDataItemRuleDTOList);
        for(PmtRuleDataItemRuleDTO pmtRule : pmtRuleDataItemRuleDTOList){
            List<ColumnCenterDataSetItemRuleDTO> itemList = pmtRule.getDataSetItemRuleDTOList();
            for(ColumnCenterDataSetItemRuleDTO item : itemList){
                Long startTime = item.getDataRule().getItemScheduleStartTime().getTime();
                Long endTime = item.getDataRule().getItemScheduleEndTime().getTime();
                if(startTime <= hitStartTime && endTime >= (hitEndTime-1)){
                    hitpmtRuleDataItemRuleDTOList.add(item);
                }
            }
        }
        LOGGER.info("****LimitTimeOriginDataItemQueryExtPt hitpmtRuleDataItemRuleDTOList.size()***"+hitpmtRuleDataItemRuleDTOList.size());
        LOGGER.info("****LimitTimeOriginDataItemQueryExtPt hitpmtRuleDataItemRuleDTOList***"+hitpmtRuleDataItemRuleDTOList);
        originDataDTO.setResult(aldInfoUtil.buildItemList(hitpmtRuleDataItemRuleDTOList));
        tacLogger.info("****LimitTimeOriginDataItemQueryExtPt originDataDTO.getResult()***"+originDataDTO.getResult());
        LOGGER.info("****LimitTimeOriginDataItemQueryExtPt originDataDTO.getResult()***"+originDataDTO.getResult());
        return Flowable.just(originDataDTO);
    }
    /**
     * 获取缓存数据
     * @return
     */
    public List<PmtRuleDataItemRuleDTO>  getCacheData(){
        List<PmtRuleDataItemRuleDTO> pmtRuleList = Lists.newArrayList();
        //5个key里面一样的
        String normalTairKey = TairUtil.formatHotTairKey();
        Result<DataEntry> rst = tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager().get(NAME_SPACE,normalTairKey);
        if(rst == null || !rst.isSuccess() || rst.getValue() == null || rst.getValue().getValue() == null){
            return pmtRuleList;
        }
        DataEntry dataEntry = rst.getValue();
        pmtRuleList = (List<PmtRuleDataItemRuleDTO>)dataEntry.getValue();
        return pmtRuleList;
    }

}
