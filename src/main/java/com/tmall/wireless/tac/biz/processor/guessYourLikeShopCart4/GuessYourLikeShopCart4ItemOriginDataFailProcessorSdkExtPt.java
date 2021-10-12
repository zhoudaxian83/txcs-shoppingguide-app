package com.tmall.wireless.tac.biz.processor.guessYourLikeShopCart4;

import com.ali.unit.rule.util.lang.CollectionUtils;
import com.alibaba.common.logging.Logger;
import com.alibaba.common.logging.LoggerFactory;
import com.alibaba.fastjson.JSON;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.impl.mc.MultiClusterTairManager;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.biz.extensions.item.origindata.DefaultItemOriginDataFailProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataFailProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderContentTypeEnum;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.MapUtil;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created from template by 程斐斐 on 2021-09-28 15:56:22.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "guessYourLikeShopCart4"
)
public class GuessYourLikeShopCart4ItemOriginDataFailProcessorSdkExtPt extends DefaultItemOriginDataFailProcessorSdkExtPt implements ItemOriginDataFailProcessorSdkExtPt {

    @Autowired
    TairFactorySpi tairFactorySpi;

    private static final int nameSpace = 184;
    /**
     * 打底商品最大数量
     **/
    private static final int needSize = 50;
    @Autowired
    TacLoggerImpl tacLogger;

    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {
        tacLogger.info("元数据处理失败扩展点将处理信息");
        OriginDataDTO<ItemEntity> originDataDTO = originDataProcessRequest.getItemEntityOriginDataDTO();
        boolean isSuccess = checkSuccess(originDataDTO);
        HadesLogUtil.stream(ScenarioConstantApp.GUESS_YOU_LIKE_SHOP_CART)
                .kv("GuessYourLikeShopCart4ItemOriginDataFailProcessorSdkExtPt", "process")
                .kv("isSuccess", String.valueOf(isSuccess))
                .info();
        if (isSuccess) {
            tacLogger.info("元数据处理失败扩展点返回成功");
            return originDataDTO;
        }

        String sKey =Optional.of(originDataProcessRequest).map(OriginDataProcessRequest::getSgFrameworkContextItem)
                .map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getPmtParams).map(PmtParams::getModuleId).orElse("153");
        MultiClusterTairManager multiClusterTairManager = tairFactorySpi.getOriginDataFailProcessTair()
                .getMultiClusterTairManager();

        Result<DataEntry> labelSceneResult = multiClusterTairManager.prefixGet(nameSpace,
                ScenarioConstantApp.GUESS_YOU_LIKE_SHOP_CART, sKey);

        if (labelSceneResult == null) {
            HadesLogUtil.stream(ScenarioConstantApp.GUESS_YOU_LIKE_SHOP_CART)
                    .kv("GuessYourLikeShopCart4ItemOriginDataFailProcessorSdkExtPt", "process")
                    .kv("labelSceneResult", "tair打底数据获取失败")
                    .info();
            tacLogger.info("元数据处理失败扩展点tair打底数据获取失败");
            return originDataDTO;
        }
        if (!labelSceneResult.isSuccess() || labelSceneResult.getValue() == null || labelSceneResult.getValue()
                .getValue() == null) {
            HadesLogUtil.stream(ScenarioConstantApp.GUESS_YOU_LIKE_SHOP_CART)
                    .kv("GuessYourLikeShopCart4ItemOriginDataFailProcessorSdkExtPt", "process")
                    .kv("labelSceneResult", JSON.toJSONString(labelSceneResult))
                    .info();
            tacLogger.info("元数据处理失败扩展点tair打底数据获取内容为空");
            originDataDTO.setHasMore(false);
            return originDataDTO;
        }

        List<Long> itemIdList = JSON.parseArray(String.valueOf(labelSceneResult.getValue().getValue()) ,Long.class);
        if (CollectionUtils.isEmpty(itemIdList)) {
            return originDataDTO;
        }
        OriginDataDTO<ItemEntity> baseOriginDataDTO = buildOriginDataDTO(itemIdList, originDataProcessRequest
                .getSgFrameworkContextItem());
        HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM)
                .kv("GuessYourLikeShopCart4ItemOriginDataFailProcessorSdkExtPt", "process")
                .kv("baseOriginDataDTO.getResult().size()", String.valueOf(baseOriginDataDTO.getResult().size()))
                .info();
        tacLogger.info("元数据处理失败扩展点tair打底数据获取成功");
        return baseOriginDataDTO;

    }

    public OriginDataDTO<ItemEntity> buildOriginDataDTO(List<Long> itemIdList, SgFrameworkContextItem contextItem) {
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
        if (itemEntitys.size() > needSize) {
            originDataDTO.setResult(itemEntitys.subList(0, needSize));
        } else {
            originDataDTO.setResult(itemEntitys);
        }
        originDataDTO.setIndex(0);
        originDataDTO.setHasMore(false);
        originDataDTO.setPvid("");
        originDataDTO.setScm("1007.0.0.0");
        return originDataDTO;
    }

    public boolean checkSuccess(OriginDataDTO<ItemEntity> originDataDTO) {

        return originDataDTO != null && CollectionUtils.isNotEmpty(originDataDTO.getResult());
    }

    private boolean isOneHour(SgFrameworkContextItem contextItem) {
        Long oneHourStore = Optional.of(contextItem).map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getLocParams).map(LocParams::getRt1HourStoreId).orElse(0L);

        return oneHourStore > 0;
    }

    private boolean isO2oScene(SgFrameworkContextItem contextItem) {
        String contentType = MapUtil.getStringWithDefault(contextItem.getRequestParams(),
                RequestKeyConstantApp.CONTENT_TYPE, RenderContentTypeEnum.b2cNormalContent.getType());

        return RenderContentTypeEnum.checkO2OContentType(contentType);
    }
}