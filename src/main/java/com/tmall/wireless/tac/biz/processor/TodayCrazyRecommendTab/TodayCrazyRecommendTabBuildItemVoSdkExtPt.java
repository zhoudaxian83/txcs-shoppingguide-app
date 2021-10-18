package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.tcls.mkt.atmosphere.model.response.ItemPromotionResp;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ErrorCode;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemInfoBySourceDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemInfoDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.O2oType;
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.captain.ItemInfoBySourceCaptainDTO;
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.tpp.ItemInfoBySourceTppDTO;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.CommonConstant;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.model.ItemLimitDTO;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.service.TodayCrazyTairCacheService;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.common.VoKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.wzt.constant.Constant;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.*;

@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB
)
public class TodayCrazyRecommendTabBuildItemVoSdkExtPt extends Register implements BuildItemVoSdkExtPt {
    @Autowired
    TacLoggerImpl tacLogger;

    @Autowired
    TodayCrazyTairCacheService todayCrazyTairCacheService;

    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        if (buildItemVoRequest == null || buildItemVoRequest.getItemInfoDTO() == null) {
            return Response.fail(ErrorCode.PARAMS_ERROR);
        }
        Map<String, Object> userParams = buildItemVoRequest.getContext().getUserParams();
        ItemEntityVO itemEntityVO = new ItemEntityVO();
        String umpChannel = MapUtil.getStringWithDefault(userParams, VoKeyConstantApp.UMP_CHANNEL,
                VoKeyConstantApp.CHANNEL_KEY);
        itemEntityVO.put("contentType", 0);
        HashMap<String, String> temIdAndCacheKeyMap = todayCrazyTairCacheService.getItemIdAndCacheKey(userParams);
        ItemInfoDTO itemInfoDTO = buildItemVoRequest.getItemInfoDTO();
        itemEntityVO.setItemId(Optional.of(itemInfoDTO).map(ItemInfoDTO::getItemEntity).map(ItemEntity::getItemId).orElse(0L));
        itemEntityVO.setO2oType(Optional.of(itemInfoDTO).map(ItemInfoDTO::getItemEntity).map(ItemEntity::getO2oType).orElse(O2oType.B2C.name()));
        String itemDesc = null;
        String originScm = "";
        String itemUrl = "";
        String specifications = "";
        String reservePrice = "";
        Map<String, Object> attachments = null;
        Set<Integer> singleFreeShipSet = new HashSet<>();
        Map<String, String> trackPoint = Maps.newHashMap();
        for (String s : itemInfoDTO.getItemInfos().keySet()) {
            ItemInfoBySourceDTO itemInfoBySourceDTO = itemInfoDTO.getItemInfos().get(s);
            if (itemInfoBySourceDTO instanceof ItemInfoBySourceCaptainDTO) {
                ItemInfoBySourceCaptainDTO itemInfoBySourceCaptainDTO = (ItemInfoBySourceCaptainDTO) itemInfoBySourceDTO;
                specifications = itemInfoBySourceCaptainDTO.getItemDTO().getSpecDetail();
                itemEntityVO.put("specifications", specifications);
                itemUrl = Optional.of(itemInfoBySourceCaptainDTO)
                        .map(ItemInfoBySourceCaptainDTO::getItemDTO)
                        .map(ItemDTO::getDetailUrl).orElse("");

                ItemDTO itemDTO = itemInfoBySourceCaptainDTO.getItemDTO();
                singleFreeShipSet = itemDTO.getItemTags();
                attachments = itemDTO.getAttachments();
                ItemPromotionResp itemPromotionResp = itemDTO.getItemPromotionResp();
                if (itemPromotionResp != null) {
                    itemDesc = this.buildItemDesc(itemPromotionResp);
                    reservePrice = this.getReservePrice(itemPromotionResp);

                }
            }
            if (itemInfoBySourceDTO instanceof ItemInfoBySourceTppDTO) {
                ItemInfoBySourceTppDTO itemInfoBySourceDTOOrigin = (ItemInfoBySourceTppDTO) itemInfoBySourceDTO;
                originScm = itemInfoBySourceDTOOrigin.getScm();

            }
            Map<String, String> scmKeyValue = itemInfoBySourceDTO.getScmKeyValue();
            if (MapUtils.isNotEmpty(scmKeyValue)) {
                trackPoint.putAll(scmKeyValue);
            }

            itemEntityVO.putAll(getItemVoMap(itemInfoBySourceDTO));

        }
        String scm = processScm(originScm, trackPoint);
        itemUrl = itemUrl + "&scm=" + scm;
        String cacheKey = this.getCacheKey(temIdAndCacheKeyMap, itemEntityVO.getItemId());
        itemEntityVO.put("scm", scm);
        itemEntityVO.put("itemUrl", itemUrl);
        itemEntityVO.put("reservePrice", reservePrice);
        //单品包邮
        itemEntityVO.put("isFreeShip", false);
        if (CollectionUtils.isNotEmpty(singleFreeShipSet)) {
            singleFreeShipSet.forEach(s -> {
                if (s == 458434 || s == 1670722) {
                    itemEntityVO.put("isFreeShip", true);
                }
            });
        }
        String itemType = null;
        if (cacheKey != null) {
            itemType = this.getItemType(cacheKey);
            itemEntityVO.put("itemType", this.getItemType(cacheKey));
        } else {
            String itemId = Long.toString(itemEntityVO.getItemId());
            Set<String> stringSet = temIdAndCacheKeyMap.keySet();
            if (stringSet.contains(itemId)) {
                //设置置顶或者坑位排序的排除掉
                HadesLogUtil.stream(ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB)
                        .kv("vo itemId tairKey is null", JSON.toJSONString(itemEntityVO.getItemId()))
                        .info();
                tacLogger.info("vo获取tairKey为空itemId" + itemEntityVO.getItemId());
            }
        }
        itemEntityVO.put("attachment", attachments);
        itemEntityVO.remove("attachments");
        itemEntityVO.put("itemDesc", itemDesc);
        itemEntityVO.put(VoKeyConstantApp.UMP_CHANNEL, umpChannel);
        if ("channelPriceNew".equals(itemType)) {
            this.buildLimit(itemEntityVO, userParams);
        }
        return Response.success(itemEntityVO);
    }

    private String getCacheKey(HashMap<String, String> temIdAndCacheKeyMap, Long itemId) {
        return temIdAndCacheKeyMap.get(Long.toString(itemId));
    }

    private String getItemType(String cacheKey) {
        //channelPriceNew,algorithm,promotion
        if (cacheKey.startsWith(CommonConstant.TODAY_CHANNEL_NEW_FEATURED) || cacheKey.startsWith(CommonConstant.TODAY_CHANNEL_NEW)) {
            return "channelPriceNew";
        } else if (cacheKey.startsWith(CommonConstant.TODAY_ALGORITHM)) {
            return "algorithm";
        } else {
            return "promotion";
        }

    }

    protected Map<String, Object> getItemVoMap(ItemInfoBySourceDTO itemInfoBySourceDTO) {

        return itemInfoBySourceDTO.getItemInfoVO();

    }


    private String processScm(String originScm, Map<String, String> scmKeyValue) {

        if (MapUtils.isEmpty(scmKeyValue)) {
            return originScm;
        }
        String addScm = Joiner.on("_").withKeyValueSeparator("-").join(scmKeyValue);

        return scmConvert(originScm, addScm);


    }

    public String scmConvert(String scm, String add) {
        try {

            if (StringUtils.isBlank(scm)) {
                return scm;
            }

            int index = scm.lastIndexOf("-");
            String prefixScm = scm.substring(0, index);
            String suffixScm = scm.substring(index);


            return prefixScm + "_" + add + suffixScm;
        } catch (Exception e) {
            //如果异常了就返回原来的
            LOGGER.error("scmConvertError", e);
            return scm;
        }
    }

    private void buildLimit(ItemEntityVO itemEntityVO, Map<String, Object> userParams) {
        if (!CommonConstant.LIMIT_BUY_SWITCH) {
            return;
        }
        List<ItemLimitDTO> itemLimitDTOS;
        Long itemId = (Long) itemEntityVO.get("itemId");
        Map<Long, List<ItemLimitDTO>> limitResult = this.getLimitResult(userParams);
        if (limitResult == null || CollectionUtils.isEmpty(limitResult.get(itemId))) {
            itemEntityVO.put("itemLimit", new ItemLimitDTO());
            return;
        }
        itemLimitDTOS = limitResult.get(itemId);
        /**
         * 限购信息
         */
        itemEntityVO.put("itemLimit", itemLimitDTOS.get(0));
    }

    private String getReservePrice(ItemPromotionResp itemPromotionResp) {
        if (itemPromotionResp.getUnifyPrice() == null) {
            return null;
        }
        if (itemPromotionResp.getUnifyPrice().getChaoShiPrice() == null || itemPromotionResp.getUnifyPrice().getChaoShiPrice().getPrice() == null) {
            return null;
        }
        return itemPromotionResp.getUnifyPrice().getChaoShiPrice().getPrice();
    }

    private String buildItemDesc(ItemPromotionResp itemPromotionResp) {
        try {
            if (itemPromotionResp.getUnifyPrice() == null) {
                return null;
            }
            if (itemPromotionResp.getUnifyPrice().getChaoShiPrice() == null || itemPromotionResp.getUnifyPrice().getChaoShiPrice().getPrice() == null) {
                return null;
            }

            if (itemPromotionResp.getUnifyPrice().getShowPrice() == null || itemPromotionResp.getUnifyPrice().getShowPrice().getPrice() == null) {
                return null;
            }
        } catch (Exception e) {
            tacLogger.info("异常vo" + e);
        }

        BigDecimal chaoShiPrice = new BigDecimal(itemPromotionResp.getUnifyPrice().getChaoShiPrice().getPrice());
        BigDecimal showPrice = new BigDecimal(itemPromotionResp.getUnifyPrice().getShowPrice().getPrice());
        String text = "专享补贴";
        return text + chaoShiPrice.subtract(showPrice) + "元";
    }

    private Map<Long, List<ItemLimitDTO>> getLimitResult(Map<String, Object> userParams) {
        Map<Long, List<ItemLimitDTO>> limitResult = (Map<Long, List<ItemLimitDTO>>) userParams.get(
                Constant.ITEM_LIMIT_RESULT);
        if (limitResult != null) {
            return limitResult;
        }
        return null;
    }
}
