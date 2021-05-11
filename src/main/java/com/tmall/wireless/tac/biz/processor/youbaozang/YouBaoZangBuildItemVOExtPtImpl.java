package com.tmall.wireless.tac.biz.processor.youbaozang;

import com.alibaba.cola.extension.Extension;
import com.alibaba.common.lang.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.iteminfo.source.captain.ItemInfoBySourceDTOMain;
import com.tmall.txcs.biz.supermarket.iteminfo.source.origindate.ItemInfoBySourceDTOOrigin;
import com.tmall.txcs.gs.framework.extensions.buildvo.BuildItemVOExtPt;
import com.tmall.txcs.gs.framework.extensions.buildvo.BuildItemVoRequest;
import com.tmall.txcs.gs.framework.model.ErrorCode;
import com.tmall.txcs.gs.framework.model.ItemEntityVO;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.model.spi.model.ItemDataDTO;
import com.tmall.txcs.gs.model.spi.model.ItemInfoBySourceDTO;
import com.tmall.txcs.gs.model.spi.model.ItemInfoDTO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.Na;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by yangqing.byq on 2021/5/11.
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENARIO_YOU_BAO_ZANG)
@Service
public class YouBaoZangBuildItemVOExtPtImpl implements BuildItemVOExtPt {


    public static final String NATIONAL_CODE = "法国;https://gw.alicdn.com/imgextra/i1/O1CN01YybSko1lEx2fyvkeh_!!6000000004788-2-tps-36-36.png;20003,110072,20109";
    private static Map<String, String> BRAND_ID_TO_NATIONAL_FLAG = Maps.newHashMap();

    static {
        String[] split = NATIONAL_CODE.split(";");
        String flag = split[1];
        String s = split[2];
        List<String> brandList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(s);
        List<String> brandIdList = brandList.stream().filter(StringUtil::isNumeric).collect(Collectors.toList());
        brandIdList.forEach(brandId -> BRAND_ID_TO_NATIONAL_FLAG.put(brandId, flag));
    }
    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {

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
                ItemInfoBySourceDTOMain itemInfoBySourceDTOMain = (ItemInfoBySourceDTOMain) itemInfoBySourceDTO;
                itemUrl = Optional.of(itemInfoBySourceDTOMain)
                        .map(ItemInfoBySourceDTOMain::getItemDTO)
                        .map(ItemDataDTO::getDetailUrl)
                        .orElse("");
                itemEntityVO.put("treasureManPoint", getTreasureManPoint(itemInfoBySourceDTOMain));

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
        itemEntityVO.put("nationalFlag", getBrandNationalFlag(itemInfoDTO));


        if (!hasMainSource) {
            return Response.fail(ErrorCode.ITEM_VO_BUILD_ERROR_HAS_NO_MAIN_SOURCE);
        }
        return Response.success(itemEntityVO);
    }

    private Object getTreasureManPoint(ItemInfoBySourceDTOMain itemInfoBySourceDTOMain) {
        Object treasureManPoint = Optional.ofNullable(itemInfoBySourceDTOMain)
                .map(ItemInfoBySourceDTOMain::getItemDTO)
                .map(ItemDataDTO::getAttachment)
                .map(att -> att.get("treasureManPoint"))
                .orElse(null);
        if (treasureManPoint == null) {
            return null;
        }

        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(treasureManPoint));

        return jsonObject.getString("content");
    }


    private Object getBrandNationalFlag(ItemInfoDTO itemInfoDTO) {
        String brandId = Optional
                .ofNullable(itemInfoDTO)
                .map(ItemInfoDTO::getItemEntity)
                .map(ItemEntity::getBrandId)
                .orElse("");
        if (StringUtils.isEmpty(brandId)) {
            return "";
        }

        return BRAND_ID_TO_NATIONAL_FLAG.get(brandId);
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
