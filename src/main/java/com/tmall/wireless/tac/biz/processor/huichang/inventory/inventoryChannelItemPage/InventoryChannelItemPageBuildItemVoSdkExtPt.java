package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelItemPage;

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

import java.util.Map;

@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
        useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
        scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_ITEM_PAGE)
public class InventoryChannelItemPageBuildItemVoSdkExtPt extends DefaultBuildItemVoSdkExtPt implements BuildItemVoSdkExtPt {
    @Autowired
    TacLogger tacLogger;

    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        tacLogger.debug("扩展点InventoryChannelItemPageBuildItemVoSdkExtPt");
        Response<ItemEntityVO> result = super.process(buildItemVoRequest);
        // 给为你推荐商品打上特殊标签
        ItemEntityVO itemEntityVO = result.getValue();
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
    }
}
