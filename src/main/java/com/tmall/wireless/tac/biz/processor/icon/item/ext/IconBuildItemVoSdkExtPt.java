package com.tmall.wireless.tac.biz.processor.icon.item.ext;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;
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

    private static final String WILL_BUY_KEY = "";

    private static final String WILL_BUY_KEY_PIC = "";


    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        Response<ItemEntityVO> response = super.process(buildItemVoRequest);
        ItemInfoBySourceDTO itemChannelInfo =  buildItemVoRequest.getItemInfoDTO().getItemInfos().get(ItemInfoSourceKey.CHANNEL);
        if (itemChannelInfo == null) {
            return response;
        }
        if (itemChannelInfo instanceof ItemInfoBySourceChannelDTO) {
            ItemInfoBySourceChannelDTO channelDTO = (ItemInfoBySourceChannelDTO) itemChannelInfo;
            //channelDTO.getItemInfoVO().get()
        }
        return response;
    }


}
