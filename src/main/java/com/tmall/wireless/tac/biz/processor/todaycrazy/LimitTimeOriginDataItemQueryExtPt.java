package com.tmall.wireless.tac.biz.processor.todaycrazy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.alibaba.cola.extension.Extension;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.MapSortUtil;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.MapUtil;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.TairUtil;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.TimeUtil;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import static com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.BannerItemInfoOriginDataItemQueryExtPt.defaultBizType;

/**
 * @author guijian
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY)
public class LimitTimeOriginDataItemQueryExtPt implements OriginDataItemQueryExtPt {

    @Autowired
    TairFactorySpi tairFactorySpi;

    @Autowired
    TacLogger tacLogger;

    private static final int NAME_SPACE = 184;
    public static final String ALD_CONTEXT = "aldContext";


    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem sgFrameworkContextItem) {
        tacLogger.info("****LimitTimeOriginDataItemQueryExtPt sgFrameworkContextItem***"+sgFrameworkContextItem);
        tacLogger.info("****LimitTimeOriginDataItemQueryExtPt sgFrameworkContextItem***"+sgFrameworkContextItem.toString());
        tacLogger.info("****LimitTimeOriginDataItemQueryExtPt sgFrameworkContextItem***"+sgFrameworkContextItem.getRequestParams());

        OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();

        Map<String, Object> params = sgFrameworkContextItem.getRequestParams();
        //第几个时间段
        int index = MapUtil.getIntWithDefault(params,"index",0);
        //ald排期信息
        Map<String, Object> contextObj = (Map<String, Object>)params.get(ALD_CONTEXT);

        /*return (List<Map<String, Object>>)contextObj.get("static_schedule_data");*/
        Map<String,String> map = Maps.newHashMap();
        LinkedHashMap<Long,Long> linkedHashMap = buildTime(map);
        List<LimitBuyDto> limitBuyDtos = Lists.newArrayList();
        //打标命中的时间段
        buildNowTime(linkedHashMap,index,limitBuyDtos);
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
        for(PmtRuleDataItemRuleDTO pmtRule : pmtRuleDataItemRuleDTOList){
            List<ColumnCenterDataSetItemRuleDTO> itemList = pmtRule.getDataSetItemRuleDTOList();
            for(ColumnCenterDataSetItemRuleDTO item : itemList){
                Long startTime = item.getDataRule().getItemScheduleStartTime().getTime()/1000;
                Long endTime = item.getDataRule().getItemScheduleEndTime().getTime()/1000;
                if(startTime <= hitStartTime && endTime >= hitEndTime){
                    hitpmtRuleDataItemRuleDTOList.add(item);
                }
            }
        }
        originDataDTO.setResult(buildItemList(hitpmtRuleDataItemRuleDTOList));
        return Flowable.just(originDataDTO);
    }
    private List<ItemEntity> buildItemList(List<ColumnCenterDataSetItemRuleDTO> columnCenterDataSetItemRuleDTOS)  {
        List<ItemEntity> result = Lists.newArrayList();
        if (CollectionUtils.isEmpty(columnCenterDataSetItemRuleDTOS)) {
            return result;
        }
        columnCenterDataSetItemRuleDTOS.forEach(columnCenterDataSetItemRuleDTO -> {
            ItemEntity itemEntity = new ItemEntity();
            itemEntity.setItemId(columnCenterDataSetItemRuleDTO.getItemId());
            itemEntity.setO2oType(columnCenterDataSetItemRuleDTO.getItemType());
            itemEntity.setBizType(defaultBizType);
            result.add(itemEntity);
        });

        return result;
    }


    /**
     * 获取缓存数据
     * @return
     */
    public List<PmtRuleDataItemRuleDTO>  getCacheData(){
        List<PmtRuleDataItemRuleDTO> pmtRuleList = Lists.newArrayList();
        Map<String, List<ColumnCenterDataSetItemRuleDTO>> todayCrazyItemMap = new HashMap<>();
        String normalTairKey = TairUtil.formatHotTairKey();
        Result<DataEntry> rst = tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager().get(NAME_SPACE,normalTairKey);
        if(rst == null || !rst.isSuccess() || rst.getValue() == null || rst.getValue().getValue() == null){
            return pmtRuleList;
        }
        DataEntry dataEntry = rst.getValue();
        pmtRuleList = (List<PmtRuleDataItemRuleDTO>)dataEntry.getValue();
        return pmtRuleList;
    }

    /**
     * 构造当前三个时间段
     * @param allTime
     * @return
     */
    public void buildNowTime(LinkedHashMap<Long,Long> allTime,int index,List<LimitBuyDto> limitBuyDtos){
        if(CollectionUtils.isEmpty(allTime)){
            return ;
        }
        Date date = new Date();
        Long nowTime = date.getTime()/1000;
        //最多取三段 map有序
        int m = 0;
        for(Map.Entry entry : allTime.entrySet()){
            if((nowTime >= (Long)entry.getKey() && nowTime < (Long)entry.getValue()) || (nowTime >= (Long)entry.getValue() && m < 3)){
                LimitBuyDto limitBuyDto = new LimitBuyDto();
                limitBuyDto.setStartTime((Long)entry.getKey());
                limitBuyDto.setStartTime((Long)entry.getValue());
                if(index == m){
                    limitBuyDto.setIsHit(true);
                }else{
                    limitBuyDto.setIsHit(false);
                }
                limitBuyDtos.add(limitBuyDto);
                m++;
            }
        }
    }
    /**
     * 构建时间段
     */
    public LinkedHashMap<Long,Long> buildTime(Map<String,String> map){
        LinkedHashMap<Long,Long> scheduleTimeMap = Maps.newLinkedHashMap();
        if(CollectionUtils.isEmpty(map)){
            return scheduleTimeMap;
        }
        //时间段排期不能低于3个，低于3个预告时间段不易确定
        if(map.size() < 3){
            return scheduleTimeMap;
        }
        //时间段排序
        Map<String,String> rsMap = MapSortUtil.sortMapByValue(map);
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf  =   new  SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        String[] values = (String[])rsMap.values().toArray();
        try {
            Long scheduleDateStart = null;
            Long scheduleDateEnd = null;
            //预告明天第一场时间段
            scheduleDateStart = sdf.parse(format.format(TimeUtil.getDate(date,1))+ " " + values[0] + ":00").getTime()/1000;
            scheduleDateEnd = sdf.parse(format.format(TimeUtil.getDate(date,1))+ " " + values[1] + ":00").getTime()/1000;
            scheduleTimeMap.put(scheduleDateStart,scheduleDateEnd);
            //预告明天第二场时间段
            scheduleDateStart = sdf.parse(format.format(TimeUtil.getDate(date,1))+ " " + values[1] + ":00").getTime()/1000;
            scheduleDateEnd = sdf.parse(format.format(TimeUtil.getDate(date,1))+ " " + values[2] + ":00").getTime()/1000;
            scheduleTimeMap.put(scheduleDateStart,scheduleDateEnd);
            for(int i=0;i<=values.length;i++){
                if(i == 0){
                    //获取昨天日期开始时间
                    scheduleDateStart = sdf.parse(format.format(TimeUtil.getDate(date,-1)) + " " + values[values.length-1] + ":00").getTime()/1000;
                    scheduleDateEnd = sdf.parse(format.format(date) + " " + values[i] + ":00").getTime()/1000;

                }else if(i == values.length) {
                    scheduleDateStart = sdf.parse(format.format(date)+ " " + values[i-1] + ":00").getTime()/1000;
                    //获取明天日期结束时间
                    scheduleDateEnd = sdf.parse(format.format(TimeUtil.getDate(date,1))+ " " + values[0] + ":00").getTime()/1000;
                }else{
                    scheduleDateStart = sdf.parse(format.format(date)+ " " + values[i-1] + ":00").getTime()/1000;
                    scheduleDateEnd = sdf.parse(format.format(date)+ " " + values[i] + ":00").getTime()/1000;
                }
                scheduleTimeMap.put(scheduleDateStart,scheduleDateEnd);
            }
        }catch (ParseException e) {
            tacLogger.info("LimitTimeOriginDataItemQueryExtPt buildTime构建时间段错误：" + e.getMessage());
            e.printStackTrace();
        }
        return scheduleTimeMap;
    }

}
