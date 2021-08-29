package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelPage;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import com.tmall.tcls.gs.sdk.biz.extensions.item.vo.DefaultBuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

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
            String itemRecommand = PageUrlUtil.getParamFromCurPageUrl(aldParams, "entryItemId", tacLogger); // 为你推荐商品
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
