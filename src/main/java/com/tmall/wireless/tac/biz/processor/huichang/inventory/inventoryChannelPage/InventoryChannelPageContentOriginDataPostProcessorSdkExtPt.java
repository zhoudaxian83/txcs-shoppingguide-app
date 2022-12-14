package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Lists;
import com.tmall.aselfcaptain.util.StackTraceUtil;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataPostProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.constant.RequestKeyConstant;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.LocParams;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.txcs.gs.model.item.BizType;
import com.tmall.txcs.gs.model.item.O2oType;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.ParseCsa;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tpp请求的后置处理。这里的处理就是在第一个场景里插入为你推荐商品
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
        useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
        scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_PAGE)
public class InventoryChannelPageContentOriginDataPostProcessorSdkExtPt extends Register implements ContentOriginDataPostProcessorSdkExtPt {
    @Autowired
    TacLogger tacLogger;

    @Override
    public OriginDataDTO<ContentEntity> process(ContentOriginDataProcessRequest contentOriginDataProcessRequest) {
        tacLogger.debug("扩展点InventoryChannelPageContentOriginDataPostProcessorSdkExtPt");
        OriginDataDTO<ContentEntity> contentEntityOriginDataDTO = new OriginDataDTO<>();
        List<ContentEntity> contentEntityList = Lists.newArrayList();
        contentEntityOriginDataDTO.setResult(contentEntityList);
        try{
            contentEntityOriginDataDTO = contentOriginDataProcessRequest.getContentEntityOriginDataDTO();
            contentEntityList = contentEntityOriginDataDTO.getResult();
            if(CollectionUtils.isEmpty(contentEntityList)) {
                throw new Exception("contentEntityList为空");
            }
            tacLogger.debug("调顺序之前场景数量" + contentEntityList.size() +"，顺序是" + JSONObject.toJSONString(contentEntityList));
            HadesLogUtil.stream("InventoryChannelPage")
                    .kv("InventoryChannelPageContentOriginDataPostProcessorSdkExtPt", "process")
                    .kv("调顺序之前场景数量", String.valueOf(contentEntityList.size()))
                    .kv("调顺序之前的顺序", JSONObject.toJSONString(contentEntityList))
                    .info();
        } catch (Exception e) {
            tacLogger.debug("场景信息解析出错：" + StackTraceUtil.stackTrace(e));
            HadesLogUtil.stream("InventoryChannelPage")
                    .kv("InventoryChannelPageContentOriginDataPostProcessorSdkExtPt", "process")
                    .kv("场景信息解析出错", StackTraceUtil.stackTrace(e))
                    .error();
            return contentEntityOriginDataDTO;
        }

        try{
            SgFrameworkContextContent sgFrameworkContextContent = contentOriginDataProcessRequest.getSgFrameworkContextContent();
            RequestContext4Ald requestContext4Ald = (RequestContext4Ald)(sgFrameworkContextContent.getTacContext());
            Map<String, Object> aldParams = requestContext4Ald.getAldParam();
            String itemRecommand = PageUrlUtil.getParamFromCurPageUrl(aldParams, "entryItemId"); // 为你推荐商品

            // 如果url有带，从url取，否则从aldParams取
            int pageIndex = Optional.ofNullable(PageUrlUtil.getParamFromCurPageUrl(aldParams, "pageIndex")).map(Integer::valueOf).orElse(MapUtil.getIntWithDefault(aldParams, "pageIndex", 0));
            // Todo pageIndex 改成第0页
            if(StringUtils.isNotBlank(itemRecommand) && pageIndex == 0) { // 第一页的第一个场景需要插入为你推荐商品以及过滤为你推荐商品
                ItemEntity itemRecommandEntity = new ItemEntity();
                itemRecommandEntity.setItemId(Long.valueOf(itemRecommand));
                String locType = PageUrlUtil.getParamFromCurPageUrl(aldParams, "locType");
                // 获取详细的履约类型。例如O2O场景下的半日达，一小时达
                String detailLocType = getDetailLocType(locType, aldParams);
                itemRecommandEntity.setO2oType(detailLocType);
                itemRecommandEntity.setBusinessType(detailLocType);
                itemRecommandEntity.setBizType(BizType.SM.getCode());

                if(CollectionUtils.isNotEmpty(contentEntityList)) {
                    // 第一个场景的商品列表
                    List<ItemEntity> itemEntityList = Optional.ofNullable(contentEntityList.get(0).getItems()).orElse(new ArrayList<ItemEntity>());
                    List<ItemEntity> newItemEntityList = Lists.newArrayList();
                    newItemEntityList.add(itemRecommandEntity);
                    // 将商品列表中 和 为你推荐商品 重复的商品过滤掉
                    for(ItemEntity itemEntity: itemEntityList) {
                        if(!itemEntity.getItemId().equals(itemRecommandEntity.getItemId())) {
                            newItemEntityList.add(itemEntity);
                        }
                    }
                    contentEntityList.get(0).setItems(newItemEntityList);
                }
                contentEntityOriginDataDTO.setResult(contentEntityList);
            }
        } catch (Exception e) {
            tacLogger.debug("场景重排序失败,使用原来顺序" + StackTraceUtil.stackTrace(e));
            HadesLogUtil.stream("InventoryChannelPage")
                    .kv("InventoryChannelPageContentOriginDataPostProcessorSdkExtPt", "process")
                    .kv("场景重排序失败", StackTraceUtil.stackTrace(e))
                    .error();
        }
        tacLogger.debug("调顺序之后场景数量是" + contentEntityOriginDataDTO.getResult().size() + "，顺序是" + JSONObject.toJSONString(contentEntityOriginDataDTO.getResult()));
        HadesLogUtil.stream("InventoryChannelPage")
                .kv("InventoryChannelPageContentOriginDataPostProcessorSdkExtPt", "process")
                .kv("调顺序之后场景数量是", String.valueOf(contentEntityOriginDataDTO.getResult().size()))
                .kv("新顺序是", JSONObject.toJSONString(contentEntityOriginDataDTO.getResult()))
                .info();
        return contentEntityOriginDataDTO;
    }

    // 获取详细的履约类型。例如O2O场景下的半日达，一小时达
    private String getDetailLocType(String locType, Map<String, Object> aldParams) {
        if(locType == null || "B2C".equals(locType)) {
            if(StringUtils.isBlank(locType)) {
                tacLogger.debug("locType是空");
            }
            return O2oType.B2C.name();
        } else {
            Long smAreaId = MapUtil.getLongWithDefault(aldParams, RequestKeyConstant.SMAREAID, 310100L);
            LocParams locParams = ParseCsa.parseCsaObj(aldParams.get(RequestKeyConstant.USER_PARAMS_KEY_CSA), smAreaId);
            if (Optional.ofNullable(locParams).map(locParams1 -> locParams1.getRt1HourStoreId()).orElse(0L) > 0){
                return O2oType.O2OOneHour.name();
            }else if(Optional.ofNullable(locParams).map(locParams1 -> locParams1.getRtHalfDayStoreId()).orElse(0L) > 0){
                return O2oType.O2OHalfDay.name();
            } else {
                return O2oType.O2O.name();
            }
        }
    }
}
