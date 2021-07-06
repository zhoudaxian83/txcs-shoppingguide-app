package com.tmall.wireless.tac.biz.processor.todaycrazy.utils;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.tmall.aselfmanager.client.columncenter.response.PmtRuleDataItemRuleDTO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.gs.spi.factory.CommonFactoryAbs;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.common.VoKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.todaycrazy.LimitTairkeyEnum;
import com.tmall.wireless.tac.client.domain.Enviroment;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author guijian
 * @date 2021/05/18
 */

public class TairUtil {

    @Autowired
    TairFactorySpi tairFactorySpi;
    @Autowired
    CommonFactoryAbs commonFactoryAbs;

    private static final int NAME_SPACE = 184;

    public String formatHotTairKey(){
        String tairKey = "";
        int num = (int) (Math.random() * 5 + 1);
        switch (num){
            case 1:
                tairKey = LimitTairkeyEnum.FLASH_SALE_HB.getKey();
                break;
            case 3:
                tairKey = LimitTairkeyEnum.FLASH_SALE_HN.getKey();
                break;
            case 4:
                tairKey = LimitTairkeyEnum.FLASH_SALE_XB.getKey();
                break;
            case 5:
                tairKey = LimitTairkeyEnum.FLASH_SALE_XN.getKey();
                break;
            default:
                tairKey = LimitTairkeyEnum.FLASH_SALE_HD.getKey();
                break;
        }
        if(commonFactoryAbs.getEnviroment().isPreline()){
            return tairKey+"_pre";
        }else if(commonFactoryAbs.getEnviroment().isOnline()){
            return tairKey;
        }else if(commonFactoryAbs.getEnviroment().isDaily()){
            return tairKey;
        }
        return tairKey+"_pre";
    }
    /**
     * 获取缓存数据
     * @return
     */
    public List<PmtRuleDataItemRuleDTO> getCacheData(){
        List<PmtRuleDataItemRuleDTO> pmtRuleList = Lists.newArrayList();
        //5个key里面一样的
        String normalTairKey = formatHotTairKey();
        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY)
            .kv("TairUtil","formatHotTairKey")
            .kv("normalTairKey", normalTairKey)
            .info();
        Result<DataEntry> rst = tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager().get(NAME_SPACE,normalTairKey);
        if(rst == null || !rst.isSuccess() || rst.getValue() == null || rst.getValue().getValue() == null){
            return pmtRuleList;
        }
        DataEntry dataEntry = rst.getValue();
        pmtRuleList = (List<PmtRuleDataItemRuleDTO>)dataEntry.getValue();
        return pmtRuleList;
    }

    /**
     * 获取channelKey 查询渠道价参数
     * @return
     */
    public String getChannelKey(){
        List<PmtRuleDataItemRuleDTO> pmtRuleDataItemRuleDTOList = getCacheData();
        if(CollectionUtils.isNotEmpty(pmtRuleDataItemRuleDTOList) && pmtRuleDataItemRuleDTOList.get(0).getPmtRuleDataSetDTO() != null){
            String promotionExtension = pmtRuleDataItemRuleDTOList.get(0).getPmtRuleDataSetDTO().getExtension();
            Map<String, Object> extensionMap = TodayCrazyUtils.parseExtension(promotionExtension, "\\|", "\\=", true);
            String channelKey = MapUtil.getStringWithDefault(extensionMap, "channelKey", VoKeyConstantApp.CHANNEL_KEY);
            return channelKey;
        }
        return VoKeyConstantApp.CHANNEL_KEY;
    }
}
