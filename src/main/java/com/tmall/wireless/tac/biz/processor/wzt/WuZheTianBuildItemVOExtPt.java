package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

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
import com.tmall.wireless.tac.biz.processor.wzt.model.ItemLimitDTO;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.CollectionUtils;
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
        Map<String, Object> userParams = buildItemVoRequest.getContext().getUserParams();
        ItemEntityVO itemEntityVO = new ItemEntityVO();
        itemEntityVO.put("contentType", 0);
        boolean hasMainSource = false;
        if (buildItemVoRequest.getItemInfoDTO() == null) {
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
        this.buildLimit(itemEntityVO, userParams);
        if (!hasMainSource) {
            return Response.fail(ErrorCode.ITEM_VO_BUILD_ERROR_HAS_NO_MAIN_SOURCE);
        }
        //补全限购信息

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

    private void buildLimit(ItemEntityVO itemEntityVO, Map<String, Object> userParams) {
        List<ItemLimitDTO> itemLimitDTOS;
        Long itemId = (Long)itemEntityVO.get("itemId");
        Map<Long, List<ItemLimitDTO>> limitResult = this.getLimitResult(userParams);
        if (limitResult == null) {
            itemEntityVO.put("limit", null);
            return;
        }
        itemLimitDTOS = limitResult.get(itemId);
        /**
         * 限购信息
         */
        itemEntityVO.put("limit", itemLimitDTOS);
    }

    private Map<Long, List<ItemLimitDTO>> getLimitResult(Map<String, Object> userParams) {
        Map<Long, List<ItemLimitDTO>> limitResult = (Map<Long, List<ItemLimitDTO>>)userParams.get("itemLimitResult");
        if (limitResult != null) {
            return limitResult;
        }
        return null;
    }
}
