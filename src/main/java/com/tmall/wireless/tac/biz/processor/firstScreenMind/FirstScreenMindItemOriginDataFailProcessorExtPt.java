package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import java.util.List;
import java.util.Map;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.impl.mc.MultiClusterTairManager;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ItemFailProcessorRequest;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ItemOriginDataFailProcessorExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author guijian
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM)
@Service
public class FirstScreenMindItemOriginDataFailProcessorExtPt implements ItemOriginDataFailProcessorExtPt {

    Logger LOGGER = LoggerFactory.getLogger(FirstScreenMindItemOriginDataFailProcessorExtPt.class);

    @Autowired
    TairFactorySpi tairFactorySpi;
    @Autowired
    TacLogger tacLogger;

    private static final int nameSpace = 184;
    /*场景商品兜底缓存前缀*/
    private static final String pKey = "txcs_scene_item_collection_v1";


    @Override
    public OriginDataDTO<ItemEntity> process(ItemFailProcessorRequest itemFailProcessorRequest) {
        Map<String, Object> requestParams = itemFailProcessorRequest.getSgFrameworkContextItem().getRequestParams();
        OriginDataDTO<ItemEntity> originDataDTO = itemFailProcessorRequest.getItemEntityOriginDataDTO();
        tacLogger.info("FirstScreenMindItemOriginDataFailProcessorExtPt originDataDTO.getResult():"+originDataDTO.getResult());
        LOGGER.info("FirstScreenMindItemOriginDataFailProcessorExtPt originDataDTO.getResult():"+originDataDTO.getResult());
        /*boolean isSuccess = checkSuccess(originDataDTO);
        if(isSuccess){
            return originDataDTO;
        }*/
        String sKey = MapUtil.getStringWithDefault(requestParams,"moduleId","");
        MultiClusterTairManager multiClusterTairManager = tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager();
        Result<DataEntry> labelSceneResult = multiClusterTairManager.prefixGet(nameSpace,pKey,sKey);
        if(!labelSceneResult.isSuccess()){
            LOGGER.info("");
        }
        DataEntry dataEntry = labelSceneResult.getValue();
        if(dataEntry == null || dataEntry.getValue() == null){
            LOGGER.info("");
        }
        List<Long> itemIdList = (List<Long>) dataEntry.getValue();
        if(CollectionUtils.isNotEmpty(itemIdList)){
            return originDataDTO;
        }
        buildOriginDataDTO(itemIdList,originDataDTO);
        tacLogger.info("FirstScreenMindItemOriginDataFailProcessorExtPt dataEntry.getValue()"+JSON.toJSONString(dataEntry.getValue()));
        LOGGER.info("FirstScreenMindItemOriginDataFailProcessorExtPt dataEntry.getValue()"+JSON.toJSONString(dataEntry.getValue()));

        return originDataDTO;
    }
    public void buildOriginDataDTO(List<Long> itemIdList,OriginDataDTO<ItemEntity> originDataDTO){
        List<ItemEntity> itemEntitys = originDataDTO.getResult();
        itemIdList.forEach(itemId -> {
            ItemEntity itemEntity = new ItemEntity();
            itemEntity.setItemId(itemId);
            itemEntitys.add(itemEntity);
        });
        tacLogger.info("FirstScreenMindItemOriginDataFailProcessorExtPt 商品打底itemEntitys:"+itemEntitys);
        LOGGER.info("FirstScreenMindItemOriginDataFailProcessorExtPt 商品打底itemEntitys:"+itemEntitys);
    }
    public boolean checkSuccess(OriginDataDTO<ItemEntity> originDataDTO){

        return originDataDTO != null && CollectionUtils.isNotEmpty(originDataDTO.getResult());
    }
}
