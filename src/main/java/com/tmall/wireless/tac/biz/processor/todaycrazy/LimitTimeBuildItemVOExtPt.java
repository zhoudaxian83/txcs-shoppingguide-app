package com.tmall.wireless.tac.biz.processor.todaycrazy;

import java.util.Map;
import java.util.Optional;
import com.alibaba.cola.extension.Extension;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.taobao.csp.hotsensor.fastjson.JSON;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.iteminfo.source.captain.ItemInfoBySourceDTOMain;
import com.tmall.txcs.biz.supermarket.iteminfo.source.origindate.ItemInfoBySourceDTOOrigin;
import com.tmall.txcs.gs.framework.extensions.buildvo.BuildItemVOExtPt;
import com.tmall.txcs.gs.framework.extensions.buildvo.BuildItemVoRequest;
import com.tmall.txcs.gs.framework.model.ErrorCode;
import com.tmall.txcs.gs.framework.model.ItemEntityVO;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.spi.model.ItemDataDTO;
import com.tmall.txcs.gs.model.spi.model.ItemInfoBySourceDTO;
import com.tmall.txcs.gs.model.spi.model.ItemInfoDTO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.common.VoKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.MapUtil;
import com.tmall.wireless.tac.biz.processor.wzt.constant.Constant;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author guijian
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY)
@Service
public class LimitTimeBuildItemVOExtPt implements BuildItemVOExtPt {

    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        ItemEntityVO itemEntityVO = new ItemEntityVO();
        Map<String,Object> userParams = buildItemVoRequest.getContext().getUserParams();
        ItemInfoDTO itemInfoDTO = buildItemVoRequest.getItemInfoDTO();
        String umpChannel = MapUtil.getStringWithDefault(userParams, VoKeyConstantApp.UMP_CHANNEL,VoKeyConstantApp.CHANNEL_KEY);
        if(userParams.get(Constant.ITEM_LIMIT_RESULT) != null){
            Object itemLimitResult = userParams.get(Constant.ITEM_LIMIT_RESULT);
            if(itemLimitResult instanceof Map){
                Long itemId =  itemInfoDTO.getItemEntity().getItemId();
                Object limitResult = ((Map<String, Object>)itemLimitResult).get(String.valueOf(itemId));
                itemEntityVO.put(Constant.ITEM_LIMIT_RESULT,limitResult);
            }
        }
        itemEntityVO.put("contentType", 0);

        if (buildItemVoRequest == null || buildItemVoRequest.getItemInfoDTO() == null) {
            return Response.fail(ErrorCode.PARAMS_ERROR);
        }

        String originScm = "";
        String itemUrl = "";
        /**是否在架**/
        boolean canBuy = false;
        /**是否已售完,true:已售完**/
        boolean sellout = false;
        /**个人限购超出或总量限购超出或无库存或下架，均出抢光标**/
        boolean isSellout = false;

        Map<String, String> trackPoint = Maps.newHashMap();

        for (String s : itemInfoDTO.getItemInfos().keySet()) {
            ItemInfoBySourceDTO itemInfoBySourceDTO = itemInfoDTO.getItemInfos().get(s);
            if (itemInfoBySourceDTO instanceof ItemInfoBySourceDTOMain) {
                ItemInfoBySourceDTOMain itemInfoBySourceDTOMain = (ItemInfoBySourceDTOMain) itemInfoBySourceDTO;
                itemUrl = Optional.of(itemInfoBySourceDTOMain)
                    .map(ItemInfoBySourceDTOMain::getItemDTO)
                    .map(ItemDataDTO::getDetailUrl)
                    .orElse("");
                ItemDataDTO itemDataDTO = itemInfoBySourceDTOMain.getItemDTO();
                canBuy = itemDataDTO.isCanBuy();
                sellout = itemDataDTO.isSellOut();
            }
            if (itemInfoBySourceDTO instanceof ItemInfoBySourceDTOOrigin) {
                ItemInfoBySourceDTOOrigin itemInfoBySourceDTOOrigin = (ItemInfoBySourceDTOOrigin) itemInfoBySourceDTO;
                originScm = itemInfoBySourceDTOOrigin.getScm();

            }
            Map<String, String> scmKeyValue = itemInfoBySourceDTO.getScmKeyValue();
            if (MapUtils.isNotEmpty(scmKeyValue)) {
                trackPoint.putAll(scmKeyValue);
            }


            itemEntityVO.putAll(itemInfoBySourceDTO.getItemInfoVO());

        }



        String scm = processScm(originScm, trackPoint);
        itemUrl = itemUrl + "&scm=" + scm;
        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY)
            .kv("canBuy", String.valueOf(canBuy))
            .kv("sellout", String.valueOf(sellout))
            .kv("itemLimitResult", JSON.toJSONString(itemEntityVO.get(Constant.ITEM_LIMIT_RESULT)))
            .info();
        itemEntityVO.put("scm", scm);
        itemEntityVO.put("itemUrl", itemUrl);
        itemEntityVO.put("soldOut",getSoldOut(itemEntityVO,canBuy,sellout));
        itemEntityVO.put(VoKeyConstantApp.UMP_CHANNEL,umpChannel);

        return Response.success(itemEntityVO);
    }

    /**
     * 是否抢光打标：个人限购超出或总量限购超出或无库存或下架，均出抢光标
     * @param itemEntityVO
     * @param canBuy
     * @param sellout
     * @return
     */
    public boolean getSoldOut(ItemEntityVO itemEntityVO,boolean canBuy,boolean sellout){
        boolean soldOut = false;
        /**总量限售**/
        int totalLimit = 0;
        /**已售总量**/
        int usedCount = 0;
        /**个人限售**/
        int userLimit = 0;
        /**个人已购数量**/
        int userUsedCount = 0;
        if(itemEntityVO.get(Constant.ITEM_LIMIT_RESULT) != null && itemEntityVO.getJSONArray(Constant.ITEM_LIMIT_RESULT).getJSONObject(0) instanceof Map){
            Map<String, Object> itemLimitResult = (Map<String, Object>)itemEntityVO.getJSONArray(Constant.ITEM_LIMIT_RESULT).getJSONObject(0);
            totalLimit = MapUtil.getIntWithDefault(itemLimitResult,"totalLimit",0);
            usedCount = MapUtil.getIntWithDefault(itemLimitResult,"usedCount",0);
            userLimit = MapUtil.getIntWithDefault(itemLimitResult,"userLimit",0);
            userUsedCount = MapUtil.getIntWithDefault(itemLimitResult,"userUsedCount",0);

        }
        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY)
            .kv("ItemId",itemEntityVO.getItemId().toString())
            .kv("totalLimit",String.valueOf(totalLimit))
            .kv("usedCount",String.valueOf(usedCount))
            .kv("userLimit",String.valueOf(userLimit))
            .kv("userUsedCount",String.valueOf(userUsedCount))
            .info();
        if(!canBuy || sellout || usedCount >= totalLimit || userUsedCount >= userLimit){
            soldOut = true;
        }
        return soldOut;
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
}
