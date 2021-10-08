package com.tmall.wireless.tac.biz.processor.gsh.itemsetrecommend;

import java.util.Map;

import com.alibaba.fastjson.JSON;

import com.tmall.tcls.gs.sdk.biz.extensions.item.vo.DefaultBuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.ItemGroup;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.tcls.gs.sdk.framework.model.iteminfo.ItemInfoGroupResponse;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wangguohui
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_GSH,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_GSH_ITEM_SET_RECOMMEND)
public class GshItemSetRecommendBuildItemVoSdkExtPt extends DefaultBuildItemVoSdkExtPt implements BuildItemVoSdkExtPt {

    Logger logger = LoggerFactory.getLogger(GshItemSetRecommendBuildItemVoSdkExtPt.class);

    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        Response<ItemEntityVO> process = super.process(buildItemVoRequest);
        SgFrameworkContextItem sgFrameworkContextItem = buildItemVoRequest.getContext();
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)(sgFrameworkContextItem.getTacContext());
        Map<String, Object> aldContext = requestContext4Ald.getAldContext();
        Object aldCurrentResId = aldContext.get(HallCommonAldConstant.ALD_CURRENT_RES_ID);
        ItemEntityVO itemEntityVO = process.getValue();
       if(itemEntityVO != null){
           itemEntityVO.put("solutionName", "gshItemSetRecommend");
           itemEntityVO.put("currentResourceId", String.valueOf(aldCurrentResId));
       }
        return process;
    }
}
