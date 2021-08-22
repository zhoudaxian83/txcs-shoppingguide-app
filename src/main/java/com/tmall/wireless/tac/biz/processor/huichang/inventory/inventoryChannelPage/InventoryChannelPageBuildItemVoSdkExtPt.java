package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelPage;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ErrorCode;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.captain.ItemInfoBySourceCaptainDTO;
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.tpp.ItemInfoBySourceTppDTO;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

public class InventoryChannelPageBuildItemVoSdkExtPt implements BuildItemVoSdkExtPt {
    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        ItemEntityVO itemEntityVO = new ItemEntityVO();

        if (buildItemVoRequest == null || buildItemVoRequest.getItemInfoDTO() == null) {
            return Response.fail(ErrorCode.PARAMS_ERROR);
        }

        ItemInfoDTO itemInfoDTO = buildItemVoRequest.getItemInfoDTO();
        SgFrameworkContextItem sgFrameworkContextItem = buildItemVoRequest.getContext();

        itemEntityVO.setItemId(Optional.of(itemInfoDTO).map(ItemInfoDTO::getItemEntity).map(ItemEntity::getItemId).orElse(0L));
        itemEntityVO.setO2oType(Optional.of(itemInfoDTO).map(ItemInfoDTO::getItemEntity).map(ItemEntity::getO2oType).orElse(O2oType.B2C.name()));


        String originScm = "";
        String itemUrl = "";

        Map<String, String> trackPoint = Maps.newHashMap();

        for (String s : itemInfoDTO.getItemInfos().keySet()) {
            ItemInfoBySourceDTO itemInfoBySourceDTO = itemInfoDTO.getItemInfos().get(s);

            if (itemInfoBySourceDTO instanceof ItemInfoBySourceCaptainDTO) {

                ItemInfoBySourceCaptainDTO itemInfoBySourceCaptainDTO = (ItemInfoBySourceCaptainDTO) itemInfoBySourceDTO;
                itemUrl = Optional.of(itemInfoBySourceCaptainDTO)
                        .map(ItemInfoBySourceCaptainDTO::getItemDTO)
                        .map(ItemDTO::getDetailUrl).orElse("");
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

        itemEntityVO.put("scm", scm);
        itemEntityVO.put("itemUrl", itemUrl);
        // 下面是我覆盖扩展点的原因
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)(sgFrameworkContextItem.getTacContext());
        Map<String, Object> aldParams = requestContext4Ald.getParams();
        String itemRecommand = PageUrlUtil.getParamFromCurPageUrl(aldParams, null, "itemRecommand"); // 为你推荐商品
        if(StringUtils.isNotBlank(itemRecommand) && itemEntityVO.getItemId().equals(Long.valueOf(itemRecommand))) {
            itemEntityVO.put("type", "recommand");
        }

        return Response.success(itemEntityVO);
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
}
