package com.tmall.wireless.tac.biz.processor.todaycrazy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.iteminfo.request.CaptainRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.model.spi.model.DataTubeParams;
import com.tmall.txcs.gs.model.spi.model.ItemDataRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.common.VoKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.TairUtil;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author guijian
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY)
@Service
public class LimitTimeCaptainRequestExtPt implements CaptainRequestExtPt {
    
    Logger LOGGER = LoggerFactory.getLogger(LimitTimeCaptainRequestExtPt.class);

    @Autowired
    TairUtil tairUtil;
    @Autowired
    TacLogger tacLogger;

    @Override
    public ItemDataRequest process(SgFrameworkContextItem contextItem) {
        ItemDataRequest itemDataRequest = new ItemDataRequest();

        Map<String,Object> userParam = contextItem.getUserParams();
        Long storeId = MapUtil.getLongWithDefault(userParam,"captainStoreId",0L);
        String O2oType = MapUtil.getStringWithDefault(userParam,"captainO2oType", "B2C");
        String mktSceneCode = MapUtil.getStringWithDefault(userParam,"captainSceneCode","");
        List<ItemEntity> list = (List<ItemEntity>)userParam.get("captainItemEntityList");
        DataTubeParams dataTubeParams = (DataTubeParams)userParam.get("captainDataTubeParams");

        if(CollectionUtils.isEmpty(list)){
            return itemDataRequest;
        }
        itemDataRequest.setList(list);
        if(storeId > 0L){
            itemDataRequest.setStoreId(storeId);
        }
        itemDataRequest.setO2oType(O2oType);
        itemDataRequest.setUserId(Optional.ofNullable(contextItem).map(SgFrameworkContext::getUserDO).map(UserDO::getUserId).orElse(0L));
        itemDataRequest.setSmAreaId(Optional.ofNullable(contextItem).map(SgFrameworkContext::getLocParams).map(
            LocParams::getSmAreaId).orElse(0L));
        if(StringUtils.isNotEmpty(mktSceneCode)){
            itemDataRequest.setMktSceneCode(mktSceneCode);
        }
        if(dataTubeParams != null){
            itemDataRequest.setDataTubeParams(dataTubeParams);
        }
        String umpChannel = tairUtil.getChannelKey();
        itemDataRequest.setChannelKey(umpChannel);
        userParam.put(VoKeyConstantApp.UMP_CHANNEL,umpChannel);
        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY)
            .kv("userId",String.valueOf(Optional.ofNullable(contextItem).map(SgFrameworkContext::getUserDO).map(UserDO::getUserId).orElse(0L)))
            .kv("LimitTimeCaptainRequestExtPt","process")
            .kv("itemDataRequest", JSON.toJSONString(itemDataRequest))
            .info();
        return itemDataRequest;
    }
}
