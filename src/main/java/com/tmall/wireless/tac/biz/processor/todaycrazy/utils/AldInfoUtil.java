package com.tmall.wireless.tac.biz.processor.todaycrazy.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterDataSetItemRuleDTO;
import com.tmall.txcs.gs.model.item.BizType;
import com.tmall.txcs.gs.model.item.O2oType;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.wireless.tac.biz.processor.todaycrazy.model.LimitBuyDto;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

public class AldInfoUtil {
    Logger LOGGER = LoggerFactory.getLogger(AldInfoUtil.class);

    @Autowired
    TacLogger tacLogger;

    private static final String ALD_CONTEXT = "ald_context";
    private static final String ALD_PARAM = "ald_param";
    private static final String STATIC_SCHEDULE_DATA = "static_schedule_data";
    private static final String EXTPARAM = "extParam";
    private static final String INDEX = "index";
    private static final String defaultBizType = "sm";
    private static final int limitBuyNum = 3;

    /**
     * 解析ald的排期信息
     * @param params
     * @return
     */
    public Map<String,String> getAldInfo(Map<String, Object> params){
        Map<String,String> map = Maps.newHashMap();
        if(CollectionUtils.isEmpty(params)){
            return map;
        }
        if(params.get(ALD_CONTEXT) != null && params.get(ALD_CONTEXT) instanceof Map){
            LOGGER.info("****AldInfoUtil getAldInfo***"+params.get(ALD_CONTEXT));
            LOGGER.info("****AldInfoUtil getAldInfo ALD_PARAM***"+params.get(ALD_PARAM));
            Map<String, Object> contextObj = (Map<String, Object>)params.get(ALD_CONTEXT);
            if(contextObj.get(STATIC_SCHEDULE_DATA) != null && contextObj.get(STATIC_SCHEDULE_DATA) instanceof List){
                List<Map<String,Object>> staticScheduleData = (List<Map<String, Object>>)contextObj.get(STATIC_SCHEDULE_DATA);
                staticScheduleData.forEach(scheduleData -> {
                    String default_numberValue = MapUtil.getStringWithDefault(scheduleData,"default_numberValue","1");
                    String startTimeAld = MapUtil.getStringWithDefault(scheduleData,"startTime","");
                    map.put(default_numberValue,startTimeAld);
                });
            }
        }
        tacLogger.info("****AldInfoUtil map***"+map);
        LOGGER.info("****AldInfoUtil map***"+map);
        return map;
    }

    /**
     * 获取预告序号
     * @param params
     * @return
     */
    public Integer getIndex(Map<String, Object> params){
        int index = 0;
        if(CollectionUtils.isEmpty(params)){
            return index;
        }
        if(params.get(ALD_PARAM) != null && params.get(ALD_PARAM) instanceof Map){
            LOGGER.info("****AldInfoUtil getAldInfo ALD_PARAM***"+params.get(ALD_PARAM));
            Map<String, Object> paramObj = (Map<String, Object>)params.get(ALD_PARAM);
            LOGGER.info("****AldInfoUtil getAldInfo paramObj***"+paramObj);
            if(paramObj.get(EXTPARAM) != null && paramObj.get(EXTPARAM) instanceof Map){
                LOGGER.info("****AldInfoUtil paramObj.get(EXTPARAM)***"+paramObj.get(EXTPARAM));
                index = MapUtil.getIntWithDefault((Map<String, Object>)paramObj.get(EXTPARAM),INDEX,0);
            }
        }
        LOGGER.info("****AldInfoUtil index:***"+index);
        return index;
    }
    public List<ItemEntity> buildItemList(List<ColumnCenterDataSetItemRuleDTO> columnCenterDataSetItemRuleDTOS)  {
        LOGGER.info("****AldInfoUtil buildItemList***"+columnCenterDataSetItemRuleDTOS.size());
        List<ItemEntity> result = Lists.newArrayList();
        if (CollectionUtils.isEmpty(columnCenterDataSetItemRuleDTOS)) {
            return result;
        }
        columnCenterDataSetItemRuleDTOS.forEach(columnCenterDataSetItemRuleDTO -> {
            ItemEntity itemEntity = new ItemEntity();
            itemEntity.setItemId(columnCenterDataSetItemRuleDTO.getItemId());
            itemEntity.setO2oType(O2oType.B2C.name());
            itemEntity.setBizType(BizType.SM.getCode());
            itemEntity.setBusinessType(O2oType.B2C.name());
            result.add(itemEntity);
        });

        return result;
    }



    /**
     * 构造当前三个时间段
     * @param allTime
     * @return
     */
    public void buildNowTime(LinkedHashMap<Long,Long> allTime,int index,List<LimitBuyDto> limitBuyDtos){
        LOGGER.info("****AldInfoUtil buildNowTime allTime***"+allTime);
        if(CollectionUtils.isEmpty(allTime)){
            return ;
        }
        Date date = new Date();
        Long nowTime = date.getTime()/1000;
        //最多取三段 map有序
        int m = 0;
        for(Map.Entry entry : allTime.entrySet()){
            if((nowTime >= (Long)entry.getKey() && nowTime < (Long)entry.getValue()) || (nowTime <= (Long)entry.getKey() && m < limitBuyNum)){
                LimitBuyDto limitBuyDto = new LimitBuyDto();
                limitBuyDto.setStartTime((Long)entry.getKey());
                limitBuyDto.setEndTime((Long)entry.getValue());
                if(index == m){
                    limitBuyDto.setIsHit(true);
                }else{
                    limitBuyDto.setIsHit(false);
                }
                limitBuyDtos.add(limitBuyDto);
                m++;
            }
        }
        LOGGER.info("****AldInfoUtil buildNowTime limitBuyDtos***"+limitBuyDtos);
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
        Object[] values = rsMap.values().toArray();
        try {
            Long scheduleDateStart = null;
            Long scheduleDateEnd = null;
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
            //预告明天第一场时间段
            scheduleDateStart = sdf.parse(format.format(TimeUtil.getDate(date,1))+ " " + values[0] + ":00").getTime()/1000;
            scheduleDateEnd = sdf.parse(format.format(TimeUtil.getDate(date,1))+ " " + values[1] + ":00").getTime()/1000;
            scheduleTimeMap.put(scheduleDateStart,scheduleDateEnd);
            //预告明天第二场时间段
            scheduleDateStart = sdf.parse(format.format(TimeUtil.getDate(date,1))+ " " + values[1] + ":00").getTime()/1000;
            scheduleDateEnd = sdf.parse(format.format(TimeUtil.getDate(date,1))+ " " + values[2] + ":00").getTime()/1000;
            scheduleTimeMap.put(scheduleDateStart,scheduleDateEnd);
        }catch (ParseException e) {
            tacLogger.info("LimitTimeOriginDataItemQueryExtPt buildTime构建时间段错误：" + e.getMessage());
            LOGGER.info("LimitTimeOriginDataItemQueryExtPt buildTime构建时间段错误：" + e.getMessage());
            e.printStackTrace();
        }
        LOGGER.info("LimitTimeOriginDataItemQueryExtPt scheduleTimeMap：" + scheduleTimeMap);
        return scheduleTimeMap;
    }

}
