package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.Map;
import java.util.Optional;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
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
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/14 10:26
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.WU_ZHE_TIAN)
@Service
public class WuZheTianBuildItemVOExtPt implements BuildItemVOExtPt {

    Logger LOGGER = LoggerFactory.getLogger(WuZheTianBuildItemVOExtPt.class);
    private static final String ITEM_URL_SUB = "https://detail.tmall.com/item.htm?id=";

    @Autowired
    TacLogger tacLogger;

    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        JSONObject getItemLimitResult = (JSONObject)buildItemVoRequest.getContext().getUserParams().get("getItemLimitResult");
        tacLogger.info("VO拿到的限购数据：" + JSON.toJSONString(getItemLimitResult));
        tacLogger.info("执行了扩展VO-VO入参数据：" + JSON.toJSONString(buildItemVoRequest));
        ItemEntityVO itemEntityVO = new ItemEntityVO();
        itemEntityVO.put("contentType", 0);
        boolean hasMainSource = false;
        if (buildItemVoRequest == null || buildItemVoRequest.getItemInfoDTO() == null) {
            return Response.fail(ErrorCode.PARAMS_ERROR);
        }
        ItemInfoDTO itemInfoDTO = buildItemVoRequest.getItemInfoDTO();

        String originScm = "";
        String itemUrl = "";

        Map<String, String> trackPoint = Maps.newHashMap();
        for (String s : itemInfoDTO.getItemInfos().keySet()) {
            ItemInfoBySourceDTO itemInfoBySourceDTO = itemInfoDTO.getItemInfos().get(s);
            if (itemInfoBySourceDTO instanceof ItemInfoBySourceDTOMain) {
                ItemInfoBySourceDTOMain itemInfoBySourceDTOMain = (ItemInfoBySourceDTOMain)itemInfoBySourceDTO;
                itemUrl = Optional.of(itemInfoBySourceDTOMain)
                    .map(ItemInfoBySourceDTOMain::getItemDTO)
                    .map(ItemDataDTO::getDetailUrl)
                    .orElse("");

                hasMainSource = true;
            }
            if (itemInfoBySourceDTO instanceof ItemInfoBySourceDTOOrigin) {
                ItemInfoBySourceDTOOrigin itemInfoBySourceDTOOrigin = (ItemInfoBySourceDTOOrigin)itemInfoBySourceDTO;
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

        itemEntityVO.put("scm", scm);
        itemEntityVO.put("itemUrl", itemUrl);
        ////skuId;
        //itemEntityVO.put("skuId","测试");
        //
        ////总体限购
        //itemEntityVO.put("totalLimit","测试");
        //
        ////已经售卖的件数
        //itemEntityVO.put("usedCount","测试");
        //
        ////用户限购信息
        //itemEntityVO.put("userLimit","测试");
        //
        ////用户已经消费
        //itemEntityVO.put("userUsedCount","测试");

        if (!hasMainSource) {
            return Response.fail(ErrorCode.ITEM_VO_BUILD_ERROR_HAS_NO_MAIN_SOURCE);
        }
        tacLogger.info("执行了扩展VO-结果打印：" + JSON.toJSONString(itemEntityVO));
        return Response.success(itemEntityVO);
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
