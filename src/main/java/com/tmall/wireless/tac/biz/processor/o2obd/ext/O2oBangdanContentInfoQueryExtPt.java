package com.tmall.wireless.tac.biz.processor.o2obd.ext;

import com.alibaba.cola.extension.Extension;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.tmall.aselfcommon.model.gcs.enums.GcsMarketChannel;
import com.tmall.aselfcommon.model.scene.domain.TairSceneDTO;
import com.tmall.aselfcommon.model.scene.enums.SceneType;
import com.tmall.aselfcommon.model.scene.valueobject.SceneDetailValue;
import com.tmall.txcs.gs.framework.extensions.content.ContentInfoQueryExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.content.ContentInfoDTO;
import com.tmall.txcs.gs.model.model.dto.ContentEntity;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.FrontBackMapEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderContentTypeEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderErrorEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.model.content.SubContentModel;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderCheckUtil;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderLangUtil;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 内容补全
 * @author haixiao.zhang
 * @date 2021/6/23
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_O2O,
    scenario = ScenarioConstantApp.O2O_BANG_DAN)
@Service
public class O2oBangdanContentInfoQueryExtPt implements ContentInfoQueryExtPt {


    @Resource
    TairFactorySpi tairFactorySpi;

    private static final int labelSceneNamespace = 184;

    @Override
    public Flowable<Response<Map<Long, ContentInfoDTO>>> process(SgFrameworkContextContent sgFrameworkContextContent) {
        /*场景详情缓存前缀*/
        String pKey = "txcs_scene_detail_v2";
        Map<Long, ContentInfoDTO> contentDTOMap = Maps.newHashMap();
        try {
            List<ContentEntity> contentEntities  = Optional.of(sgFrameworkContextContent).map(SgFrameworkContextContent::getContentEntityOriginDataDTO).map(OriginDataDTO::getResult).orElse(Lists
                .newArrayList());
            List<String> sKeyList = new ArrayList<>();
            for (ContentEntity contentEntity : contentEntities) {
                sKeyList.add(pKey + "_" + contentEntity.getContentId());
            }
            Result<List<DataEntry>> mgetResult = tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager().mget(labelSceneNamespace, sKeyList);
            LOGGER.info("***********mgetResult.getValue()*******:"+mgetResult.getValue());
            if (!mgetResult.isSuccess() || CollectionUtils.isEmpty(mgetResult.getValue())) {
                return Flowable.just(Response.fail(""));
            }
            List<DataEntry> dataEntryList = mgetResult.getValue();
            Map<Long, TairSceneDTO> tairResult = Maps.newHashMap();
            //循环遍历获取结果
            dataEntryList.forEach(dataEntry -> {
                Object tairKey = dataEntry.getKey();
                String tairKeyStr = String.valueOf(tairKey);
                String[] s = tairKeyStr.split("_");
                String contentId = s[s.length - 1];
                TairSceneDTO value = (TairSceneDTO) dataEntry.getValue();
                tairResult.put(Long.valueOf(contentId), value);
            });
            for(ContentEntity contentEntity : contentEntities){
                Long contentId = contentEntity.getContentId();
                TairSceneDTO tairSceneDTO = tairResult.get(contentId);
                /**如果内容后台返回的补全内容为空，那么把这个内容过滤掉，并且日志记录*/
                if(!tairResult.containsKey(contentId) || tairSceneDTO == null){
                    continue;
                }
                ContentInfoDTO contentDTO = new ContentInfoDTO();
                Map<String, Object> contentInfo = Maps.newHashMap();
                contentInfo.put("contentId",tairSceneDTO.getId());
                contentInfo.put("contentTitle",tairSceneDTO.getTitle());
                contentInfo.put("contentSubtitle",tairSceneDTO.getSubtitle());
                contentInfo.put("itemSetIds", getItemSetIds(tairSceneDTO));
                Map<String, Object> tairPropertyMap = tairSceneDTO.getProperty();
                //前后端映射  首页改版、逛超市映射字段相同
                for(FrontBackMapEnum frontBackMapEnum : FrontBackMapEnum.values()){
                    contentInfo.put(frontBackMapEnum.getFront(),tairPropertyMap.get(frontBackMapEnum.getBack()));
                }
                /**内容类型*/
                String type = SceneType.of(tairSceneDTO.getType()).name();
                String marketChannel = GcsMarketChannel.of(tairSceneDTO.getMarketChannel()).name();
                /**后台没有类型，那么就直接返回普通场景打底*/
                if(RenderCheckUtil.StringEmpty(type) || RenderCheckUtil.StringEmpty(marketChannel)){
                    contentInfo.put("contentType",RenderContentTypeEnum.getBottomContentType());
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

                contentDTO.setContentInfo(contentInfo);
                contentDTOMap.put(contentId,contentDTO);
            }
        }catch (Exception e){
            LOGGER.info(RenderErrorEnum.contentBatchTairExc.getCode(), RenderErrorEnum.contentBatchTairExc.getMessage());
            return Flowable.just(Response.fail(RenderErrorEnum.contentBatchTairExc.getCode()));
        }
        return Flowable.just(Response.success(contentDTOMap));
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
    private static List<SubContentModel> buildSubContentBaseInfoV2(Map<String, Object> contentInfo, TairSceneDTO labelSceneContentInfo){

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
