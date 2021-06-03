package com.tmall.wireless.tac.biz.processor.firstScreenMind.common;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.tmall.aselfcommon.model.gcs.enums.GcsMarketChannel;
import com.tmall.aselfcommon.model.scene.domain.TairSceneDTO;
import com.tmall.aselfcommon.model.scene.enums.SceneType;
import com.tmall.aselfcommon.model.scene.valueobject.SceneDetailValue;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.FrontBackMapEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderContentTypeEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.model.content.SubContentModel;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderCheckUtil;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderLangUtil;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by yangqing.byq on 2021/5/15.
 */
public class ContentInfoSupport {
    @Resource
    TairFactorySpi tairFactorySpi;
    String CONTENT_TAIR_INFO_KEY = "txcs_scene_detail_v2_";

    @Autowired
    TacLoggerImpl tacLogger;

    private static final int labelSceneNamespace = 184;

    /**
     * 获取tair数据
     * @param contentIdList
     * @return
     */
    public Map<Long, TairSceneDTO> getTairData(List<Long> contentIdList){
        List<String> sKeyList = Lists.newArrayList();
        for (Long contentId : contentIdList) {
            sKeyList.add(CONTENT_TAIR_INFO_KEY + contentId);
        }
        Result<List<DataEntry>> mgetResult =tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager().mget(labelSceneNamespace, sKeyList);
        Map<Long, TairSceneDTO> tairResult = Maps.newHashMap();
        if (!mgetResult.isSuccess() || CollectionUtils.isEmpty(mgetResult.getValue())) {
            return tairResult;
        }
        List<DataEntry> dataEntryList = mgetResult.getValue();
        //循环遍历获取结果
        dataEntryList.forEach(dataEntry -> {
            // txcs_scene_detail_v2_2020053172349
            Object tairKey = dataEntry.getKey();
            String tairKeyStr = String.valueOf(tairKey);
            String[] s = tairKeyStr.split("_");
            String contentId = s[s.length - 1];
            TairSceneDTO value = (TairSceneDTO) dataEntry.getValue();
            tairResult.put(Long.valueOf(contentId), value);
        });
        return tairResult;
    }

    public Map<Long, Map<String, Object>> queryContentInfoByContentIdList(List<Long> contentIdList) {
        List<String> sKeyList = new ArrayList<>();

        Map<Long, Map<String, Object>> contentInfoMap = Maps.newHashMap();
        for (Long contentId : contentIdList) {
            sKeyList.add(CONTENT_TAIR_INFO_KEY + contentId);
        }
        Result<List<DataEntry>> mgetResult =tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager().mget(labelSceneNamespace, sKeyList);
        tacLogger.info("***********FirstScreenMindContentInfoQueryExtPt mgetResult*******:"+mgetResult.toString());
        if (!mgetResult.isSuccess() || CollectionUtils.isEmpty(mgetResult.getValue())) {
            return Maps.newHashMap();
        }
        List<DataEntry> dataEntryList = mgetResult.getValue();
        Map<Long, TairSceneDTO> tairResult = Maps.newHashMap();
        //循环遍历获取结果
        dataEntryList.forEach(dataEntry -> {
            // txcs_scene_detail_v2_2020053172349
            Object tairKey = dataEntry.getKey();
            String tairKeyStr = String.valueOf(tairKey);
            String[] s = tairKeyStr.split("_");
            String contentId = s[s.length - 1];
            TairSceneDTO value = (TairSceneDTO) dataEntry.getValue();
            tairResult.put(Long.valueOf(contentId), value);
        });;
        for(Long contentId : contentIdList){
            TairSceneDTO tairSceneDTO = tairResult.get(contentId);
            /**如果内容后台返回的补全内容为空，那么把这个内容过滤掉，并且日志记录*/
            if(!tairResult.containsKey(contentId) || tairSceneDTO == null){
                tacLogger.info("批量补全内容中心信息返回为空contentId:" + contentId +",tairResult:"+tairResult);
                continue;
            }
            Map<String, Object> contentInfo = Maps.newHashMap();
            contentInfo.put("contentId",tairSceneDTO.getId());
            contentInfo.put("contentTitle",tairSceneDTO.getTitle());
            contentInfo.put("contentSubtitle",tairSceneDTO.getSubtitle());
            contentInfo.put("itemSetIds", getItemSetIds(tairSceneDTO));
            Map<String, Object> tairPropertyMap = tairSceneDTO.getProperty();
            //前后端映射
            for(FrontBackMapEnum frontBackMapEnum : FrontBackMapEnum.values()){
                contentInfo.put(frontBackMapEnum.getFront(),tairPropertyMap.get(frontBackMapEnum.getBack()));
            }
            /**内容类型*/
            String type = SceneType.of(tairSceneDTO.getType()).name();
            String marketChannel = GcsMarketChannel.of(tairSceneDTO.getMarketChannel()).name();
            /**后台没有类型，那么就直接返回普通场景打底*/
            if(RenderCheckUtil.StringEmpty(type) || RenderCheckUtil.StringEmpty(marketChannel)){
                contentInfo.put("contentType", RenderContentTypeEnum.getBottomContentType());
            }
            //b2c普通场景
            if(marketChannel.equals(GcsMarketChannel.B2C.name()) && type.equals(SceneType.NORMAL.name())){
                contentInfo.put("contentType",RenderContentTypeEnum.b2cNormalContent.getType());
                //b2c组合场景
            }else if(marketChannel.equals(GcsMarketChannel.B2C.name()) && type.equals(SceneType.COMBINE.name())){
                contentInfo.put("contentType",RenderContentTypeEnum.b2cCombineContent.getType());
                contentInfo.put("subContentModelList",buildSubContentBaseInfoV2(contentInfo,tairSceneDTO));
                //b2c品牌场景
            }else if(marketChannel.equals(GcsMarketChannel.B2C.name()) && type.equals(SceneType.BRAND.name())){
                contentInfo.put("contentType",RenderContentTypeEnum.b2cBrandContent.getType());
                //o2o组合场景
            }else if(marketChannel.equals(GcsMarketChannel.O2O.name()) && type.equals(SceneType.NORMAL.name())){
                contentInfo.put("contentType",RenderContentTypeEnum.o2oNormalContent.getType());
                //o2o品牌场景
            }else if(marketChannel.equals(GcsMarketChannel.O2O.name()) && type.equals(SceneType.COMBINE.name())){
                contentInfo.put("contentType",RenderContentTypeEnum.o2oCombineContent.getType());
                contentInfo.put("subContentModelList",buildSubContentBaseInfoV2(contentInfo,tairSceneDTO));
                //o2o品牌场景
            }else if(marketChannel.equals(GcsMarketChannel.O2O.name()) && type.equals(SceneType.BRAND.name())){
                contentInfo.put("contentType",RenderContentTypeEnum.o2oBrandContent.getType());
            } else if (type.equals(SceneType.RECIPE.name())) {
                contentInfo.put("contentType",RenderContentTypeEnum.recipeContent.getType());
            } else if (type.equals(SceneType.MEDIA.name())) {
                contentInfo.put("contentType",RenderContentTypeEnum.mediaContent.getType());
            } else if (type.equals(SceneType.BOARD.name())) {
                contentInfo.put("contentType",RenderContentTypeEnum.bangdanContent.getType());
            } else {
                //默认打底-普通场景
                contentInfo.put("contentType",RenderContentTypeEnum.getBottomContentType());
            }

            contentInfoMap.put(contentId, contentInfo);
        }
        return contentInfoMap;
    }
    /**
     * 构造置顶商品-视频场景内
     * @param topItemIds
     */
    public List<ItemEntity> buildTopItemEntityList(List<Long> topItemIds,String marketChannel){
        if (CollectionUtils.isEmpty(topItemIds)) {
            return null;
        }
        List<ItemEntity> itemEntityList = topItemIds.stream().map(itemId -> {
            ItemEntity itemEntity = new ItemEntity();
            itemEntity.setItemId(itemId);
            itemEntity.setO2oType(marketChannel);
            itemEntity.setBizType(marketChannel);
            return itemEntity;
        }).collect(Collectors.toList());

        return itemEntityList;

    }

    /**
     * 获取内容场景下的置顶商品--支持货架场景
     * @param labelSceneContentInfo
     * @return
     */
    public Map<Long,List<Long>> getTopItemIds(TairSceneDTO labelSceneContentInfo){
        Map<Long,List<Long>> topItemIdMap = Maps.newHashMap();
        List<SceneDetailValue> sceneDetailValues = Optional.ofNullable(labelSceneContentInfo)
            .map(TairSceneDTO::getDetails)
            .orElse(Lists.newArrayList());
        if (CollectionUtils.isEmpty(sceneDetailValues)) {
            return topItemIdMap;
        }
        sceneDetailValues.forEach(sceneDetailValue -> {
            Long itemsetId = sceneDetailValue.getItemsetId();
            List<Long> topItemIds = sceneDetailValue.getTopItemIds();
            topItemIdMap.put(itemsetId,topItemIds);
        });
        return topItemIdMap;
    }
    private static String getItemSetIds(TairSceneDTO labelSceneContentInfo) {

        List<SceneDetailValue> sceneDetailValues = Optional.ofNullable(labelSceneContentInfo)
                .map(TairSceneDTO::getDetails)
                .orElse(Lists.newArrayList());

        if (CollectionUtils.isEmpty(sceneDetailValues)) {
            return "";
        }

        List<Long> itemSetIds = sceneDetailValues.stream().map(SceneDetailValue::getItemsetId).collect(Collectors.toList());
        return Joiner.on(",").join(itemSetIds);

    }

    private static List<SubContentModel> buildSubContentBaseInfoV2(Map<String, Object> contentInfo,TairSceneDTO labelSceneContentInfo){

        List<SceneDetailValue> details = labelSceneContentInfo.getDetails();

        List<SubContentModel> subContentModelList = new ArrayList<>();
        for (SceneDetailValue detail : details) {

            SubContentModel sub = new SubContentModel();
            sub.setSubContentId(detail.getDetailId());
            sub.setSubContentTitle(detail.getTitle());
            if(contentInfo.get("subContentType") != null && StringUtils.isNotEmpty(String.valueOf(contentInfo.get("subContentType")))){
                sub.setSubContentType(String.valueOf(contentInfo.get("subContentType")));
            }
            sub.setItemSetIds(RenderLangUtil.safeString(detail.getItemsetId()));
            subContentModelList.add(sub);
        }
        return subContentModelList;
    }
}
