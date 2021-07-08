package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.impl.mc.MultiClusterTairManager;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ItemFailProcessorRequest;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ItemOriginDataFailProcessorExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.item.BizType;
import com.tmall.txcs.gs.model.item.O2oType;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderContentTypeEnum;
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
    TacLogger tacLogger;
    @Autowired
    TairFactorySpi tairFactorySpi;

    private static final int nameSpace = 184;
    /**场景商品兜底缓存前缀**/
    private static final String pKey = "txcs_scene_item_collection_v1";
    /**打底商品最大数量**/
    private static final int needSize = 50;


    @Override
    public OriginDataDTO<ItemEntity> process(ItemFailProcessorRequest itemFailProcessorRequest) {
        Map<String, Object> requestParams = itemFailProcessorRequest.getSgFrameworkContextItem().getRequestParams();
        OriginDataDTO<ItemEntity> originDataDTO = itemFailProcessorRequest.getItemEntityOriginDataDTO();
        boolean isSuccess = checkSuccess(originDataDTO);
        HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM)
            .kv("FirstScreenMindItemOriginDataFailProcessorExtPt","process")
            .kv("isSuccess",String.valueOf(isSuccess))
            .info();
        if(isSuccess){
            return originDataDTO;
        }
        String sKey = MapUtil.getStringWithDefault(requestParams,"moduleId","");
        MultiClusterTairManager multiClusterTairManager = tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager();
        Result<DataEntry> labelSceneResult = multiClusterTairManager.prefixGet(nameSpace,pKey,sKey);
        if(!labelSceneResult.isSuccess()){
            HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM)
                .kv("FirstScreenMindItemOriginDataFailProcessorExtPt","process")
                .kv("labelSceneResult", JSON.toJSONString(labelSceneResult))
                .info();
            return originDataDTO;
        }
        DataEntry dataEntry = labelSceneResult.getValue();
        if(dataEntry == null || dataEntry.getValue() == null){
            HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM)
                .kv("FirstScreenMindItemOriginDataFailProcessorExtPt","process")
                .kv("dataEntry", "tair：dataEntry打底数据为空")
                .info();
            return originDataDTO;
        }
        List<Long> itemIdList = (List<Long>) dataEntry.getValue();
        if(CollectionUtils.isEmpty(itemIdList)){
            return originDataDTO;
        }
        OriginDataDTO<ItemEntity> baseOriginDataDTO = buildOriginDataDTO(itemIdList,itemFailProcessorRequest.getSgFrameworkContextItem());
        HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM)
            .kv("FirstScreenMindItemOriginDataFailProcessorExtPt","process")
            .kv("baseOriginDataDTO.getResult().size()", String.valueOf(baseOriginDataDTO.getResult().size()))
            .info();
        return baseOriginDataDTO;
    }
    public OriginDataDTO<ItemEntity> buildOriginDataDTO(List<Long> itemIdList,SgFrameworkContextItem contextItem){
        OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();
        boolean isO2oScene = isO2oScene(contextItem);

        String businessType;
        String bizType;
        String o2oType;

        if (isO2oScene) {
            businessType = O2oType.O2O.name();
            bizType = BizType.SM.getCode();
            if (isOneHour(contextItem)) {
                o2oType = O2oType.O2OOneHour.name();
            } else {
                o2oType = O2oType.O2OHalfDay.name();
            }
        } else {
            businessType = O2oType.B2C.name();
            bizType = BizType.SM.getCode();
            o2oType = O2oType.B2C.name();
        }
        List<ItemEntity> itemEntitys = itemIdList.stream().map(itemId -> {
            ItemEntity itemEntity = new ItemEntity();
            itemEntity.setItemId(itemId);
            itemEntity.setBizType(bizType);
            itemEntity.setBusinessType(businessType);
            itemEntity.setO2oType(o2oType);
            return itemEntity;
        }).collect(Collectors.toList());
        if(itemEntitys.size() > needSize){
            originDataDTO.setResult(itemEntitys.subList(0,needSize));
        }else{
            originDataDTO.setResult(itemEntitys);
        }
        originDataDTO.setIndex(0);
        originDataDTO.setHasMore(false);
        originDataDTO.setPvid("");
        originDataDTO.setScm("1007.0.0.0");
        return originDataDTO;
    }
    public boolean checkSuccess(OriginDataDTO<ItemEntity> originDataDTO){

        return originDataDTO != null && CollectionUtils.isNotEmpty(originDataDTO.getResult());
    }
    private boolean isOneHour(SgFrameworkContextItem contextItem) {
        Long oneHourStore = Optional.of(contextItem).map(SgFrameworkContext::getLocParams).map(LocParams::getRt1HourStoreId).orElse(0L);

        return oneHourStore > 0;
    }

    private boolean isO2oScene(SgFrameworkContextItem contextItem) {
        String contentType = MapUtil.getStringWithDefault(contextItem.getRequestParams(), RequestKeyConstantApp.CONTENT_TYPE, RenderContentTypeEnum.b2cNormalContent.getType());

        return RenderContentTypeEnum.checkO2OContentType(contentType);
    }
}
