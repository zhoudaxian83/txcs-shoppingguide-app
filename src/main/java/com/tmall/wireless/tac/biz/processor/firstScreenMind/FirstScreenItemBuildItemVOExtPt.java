package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import java.util.Map;
import java.util.Optional;
import com.alibaba.cola.extension.Extension;
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
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author guijian
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM)
@Service
public class FirstScreenItemBuildItemVOExtPt implements BuildItemVOExtPt {

    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        ItemEntityVO itemEntityVO = new ItemEntityVO();
        itemEntityVO.put("contentType", 0);
        boolean hasMainSource = false;

        if (buildItemVoRequest == null || buildItemVoRequest.getItemInfoDTO() == null) {
            return Response.fail(ErrorCode.PARAMS_ERROR);
        }

        ItemInfoDTO itemInfoDTO = buildItemVoRequest.getItemInfoDTO();
        itemEntityVO.put("isTop",itemInfoDTO.getItemEntity().isTop());
        String originScm = "";
        String itemUrl = "";


        boolean canBuy = true;

        Map<String, String> trackPoint = Maps.newHashMap();

        for (String s : itemInfoDTO.getItemInfos().keySet()) {
            ItemInfoBySourceDTO itemInfoBySourceDTO = itemInfoDTO.getItemInfos().get(s);
            if (itemInfoBySourceDTO instanceof ItemInfoBySourceDTOMain) {
                ItemInfoBySourceDTOMain itemInfoBySourceDTOMain = (ItemInfoBySourceDTOMain) itemInfoBySourceDTO;
                itemUrl = Optional.of(itemInfoBySourceDTOMain)
                    .map(ItemInfoBySourceDTOMain::getItemDTO)
                    .map(ItemDataDTO::getDetailUrl)
                    .orElse("");

                canBuy = canBuy(itemInfoBySourceDTOMain);
                hasMainSource = true;
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

        itemEntityVO.put("scm", scm);
        itemEntityVO.put("itemUrl", itemUrl);

        if (!canBuy) {
            return Response.fail("ITEM_VO_BUILD_ERROR_CAN_BUY_FALSE_F");
        }

        if (!hasMainSource) {
            return Response.fail(ErrorCode.ITEM_VO_BUILD_ERROR_HAS_NO_MAIN_SOURCE);
        }
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
            //?????????????????????????????????
            LOGGER.error("scmConvertError", e);
            return scm;
        }
    }

    private boolean canBuy(ItemInfoBySourceDTOMain itemInfoBySourceDTO) {
        Boolean canBuy = Optional.of(itemInfoBySourceDTO).map(ItemInfoBySourceDTOMain::getItemDTO).map(ItemDataDTO::isCanBuy).orElse(true);
        Boolean sellOut = Optional.of(itemInfoBySourceDTO).map(ItemInfoBySourceDTOMain::getItemDTO).map(ItemDataDTO::isSellOut).orElse(false);
        return canBuy && !sellOut;

    }
}
