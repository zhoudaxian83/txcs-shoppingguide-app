package com.tmall.wireless.tac.biz.processor.icon.item.ext;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tcls.mkt.atmosphere.model.response.IconAtmosphereDTO;
import com.tcls.mkt.atmosphere.model.response.ItemPromotionResp;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.tcls.gs.sdk.biz.extensions.item.vo.DefaultBuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.biz.iteminfo.bysource.ItemInfoSourceKey;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
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
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.channel.ItemInfoBySourceChannelDTO;
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.smartui.ItemInfoBySourceSmartUiDTOSdk;
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.tpp.ItemInfoBySourceTppDTO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import org.apache.commons.collections.MapUtils;

/**
 * @author zhongwei
 * @date 2021/11/8
 */
@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.ICON_ITEM
)
public class IconBuildItemVoSdkExtPt  extends DefaultBuildItemVoSdkExtPt implements BuildItemVoSdkExtPt {

    public static final String MOST_WORTH_BUY_KEY = "mostWorthBuy";

    private static final String MOST_WORTH_BUY_PIC = "https://img.alicdn.com/imgextra/i1/O1CN01kAxxAD1aO9GC7TSzp_!!6000000003319-2-tps-216-90.png";


    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        Response<ItemEntityVO> response = super.process(buildItemVoRequest);
        ItemInfoBySourceDTO itemChannelInfo =  buildItemVoRequest.getItemInfoDTO().getItemInfos().get(ItemInfoSourceKey.CHANNEL);
        boolean isMostWorthBuying = false;
        if (itemChannelInfo instanceof ItemInfoBySourceChannelDTO) {
            ItemInfoBySourceChannelDTO channelDTO = (ItemInfoBySourceChannelDTO) itemChannelInfo;
            isMostWorthBuying = channelDTO.getChannelDataMap().containsKey(MOST_WORTH_BUY_KEY);
        }
        ItemInfoBySourceDTO itemCaptainInfo =  buildItemVoRequest.getItemInfoDTO().getItemInfos().get(ItemInfoSourceKey.CAPTAIN);
        List<IconAtmosphereDTO> newIconAtmosphereDTO = Lists.newArrayList();
        if (itemCaptainInfo instanceof ItemInfoBySourceCaptainDTO) {
            ItemInfoBySourceCaptainDTO captainDTO = (ItemInfoBySourceCaptainDTO) itemCaptainInfo;
            List<IconAtmosphereDTO> iconAtmosphereDTOList =  Optional.ofNullable(captainDTO.getItemDTO().getItemPromotionResp())
                .map(ItemPromotionResp::getIconAtmosphereList).orElse(Lists.newArrayList());
            newIconAtmosphereDTO = sortIconAtmosphereLabel(iconAtmosphereDTOList, isMostWorthBuying);
            captainDTO.getItemDTO().getItemPromotionResp().setIconAtmosphereList(newIconAtmosphereDTO);
        }
        response.getValue().put("itemAtmosphereList", newIconAtmosphereDTO);
        ItemInfoBySourceDTO smartUIDTO =  buildItemVoRequest.getItemInfoDTO().getItemInfos().get(ItemInfoSourceKey.SMART_UI);
        if (smartUIDTO instanceof ItemInfoBySourceSmartUiDTOSdk) {
            ItemInfoBySourceSmartUiDTOSdk smartUiDTOSdk = (ItemInfoBySourceSmartUiDTOSdk) smartUIDTO;
            List<String> smartUilabels = newIconAtmosphereDTO.stream().map(IconAtmosphereDTO::getIconUrl).collect(Collectors.toList());
            smartUiDTOSdk.getSmartUiInfoMap().put("timeServiceLable", smartUilabels);
        }
        return response;
    }

    /**
     * 展示0~2个标 (3个标放不下)，优先级：大促标(BigMarkDown)>值得买标(MostWorthBuy)>疯抢标(UserDefined) （若3个标都存在，则展示：大促标、值得买标）
     * @param iconAtmosphereDTOList
     * @param isMostWorthBuying
     */
    private List<IconAtmosphereDTO>  sortIconAtmosphereLabel(List<IconAtmosphereDTO> iconAtmosphereDTOList, Boolean isMostWorthBuying) {
        if (isMostWorthBuying) {
            IconAtmosphereDTO mostWorthBuyAtmosphereDTO = new IconAtmosphereDTO();
            mostWorthBuyAtmosphereDTO.setType("MostWorthBuy");
            mostWorthBuyAtmosphereDTO.setIconUrl(MOST_WORTH_BUY_PIC);
            iconAtmosphereDTOList.add(mostWorthBuyAtmosphereDTO);
        }
        return iconAtmosphereDTOList.stream()
            .sorted(Comparator.comparing(v -> {
                if (Objects.equals(v.getType(), "BigMarkDown")) {
                    return 0;
                } else if (Objects.equals(v.getType(), "MostWorthBuy")) {
                    return 1;
                } else if (Objects.equals(v.getType(), "UserDefined")) {
                    return 2;
                }
                return 3;
            }
        )).limit(2).collect(Collectors.toList());
    }

}
