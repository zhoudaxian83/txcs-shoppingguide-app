package com.tmall.wireless.tac.biz.processor.guessWhatYouLike.promotion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tmall.tcls.gs.sdk.biz.extensions.item.vo.DefaultBuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;

/**
 * @author zhongwei
 * @date 2021/12/1
 */
@SdkExtension(
    bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_SUB_PROMOTION_PAGE
)
public class GulItemSetRecommendBuildItemVoSdkExtPt extends DefaultBuildItemVoSdkExtPt implements BuildItemVoSdkExtPt {

    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        Response<ItemEntityVO> response = super.process(buildItemVoRequest);
        SgFrameworkContextItem sgFrameworkContextItem = buildItemVoRequest.getContext();
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)(sgFrameworkContextItem.getTacContext());
        Map<String, Object> aldContext = requestContext4Ald.getAldContext();
        Object aldCurrentResId = aldContext.get(HallCommonAldConstant.ALD_CURRENT_RES_ID);
        ItemEntityVO itemEntityVO = response.getValue();
        if (itemEntityVO == null) {
            return response;
        }
        itemEntityVO.put("currentResourceId", String.valueOf(aldCurrentResId));
        return response;
    }
}
