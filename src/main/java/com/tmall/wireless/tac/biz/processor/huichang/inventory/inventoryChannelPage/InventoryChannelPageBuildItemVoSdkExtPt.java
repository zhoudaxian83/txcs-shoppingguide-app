package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelPage;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.tcls.gs.sdk.biz.extensions.item.vo.DefaultBuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ErrorCode;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.captain.ItemInfoBySourceCaptainDTO;
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.tpp.ItemInfoBySourceTppDTO;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
        useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
        scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_PAGE)
public class InventoryChannelPageBuildItemVoSdkExtPt extends DefaultBuildItemVoSdkExtPt implements BuildItemVoSdkExtPt {
    @Autowired
    TacLogger tacLogger;

    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        tacLogger.debug("扩展点InventoryChannelPageBuildItemVoSdkExtPt");
        Response<ItemEntityVO> result = super.process(buildItemVoRequest);
        try{
            // 给为你推荐商品打上特殊标签
            ItemEntityVO itemEntityVO = result.getValue();
            tacLogger.debug(JSONObject.toJSONString(itemEntityVO));
            SgFrameworkContextItem sgFrameworkContextItem = buildItemVoRequest.getContext();
            RequestContext4Ald requestContext4Ald = (RequestContext4Ald)(sgFrameworkContextItem.getTacContext());
            Map<String, Object> aldParams = requestContext4Ald.getAldParam();
            String itemRecommand = PageUrlUtil.getParamFromCurPageUrl(aldParams, "itemRecommand", tacLogger); // 为你推荐商品
            if(StringUtils.isNotBlank(itemRecommand) && itemEntityVO.getItemId().equals(Long.valueOf(itemRecommand))) {
                itemEntityVO.put("isRecommand", "true");
            } else {
                itemEntityVO.put("isRecommand", "false");
            }
            result.setValue(itemEntityVO);
            return result;
        } catch (Exception e) {
            tacLogger.debug("给为你推荐商品打标签失败");
            return result;
        }
    }
}
