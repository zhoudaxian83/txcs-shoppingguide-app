package com.tmall.wireless.tac.biz.processor.gsh.itemrecommend;

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
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_GSH_ITEM_RECOMMEND)
public class GshItemRecommendBuildItemVoSdkExtPt extends DefaultBuildItemVoSdkExtPt implements BuildItemVoSdkExtPt {

    Logger logger = LoggerFactory.getLogger(GshItemRecommendBuildItemVoSdkExtPt.class);

    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        Response<ItemEntityVO> process = super.process(buildItemVoRequest);
        SgFrameworkContextItem sgFrameworkContextItem = buildItemVoRequest.getContext();
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)(sgFrameworkContextItem.getTacContext());
        Map<String, Object> aldContext = requestContext4Ald.getAldContext();
        Object aldCurrentResId = aldContext.get(HallCommonAldConstant.ALD_CURRENT_RES_ID);
        Object staticData = aldContext.get(HallCommonAldConstant.STATIC_SCHEDULE_DATA);
        Map<Long, Map<String, Object>> staticMap = new HashMap<>();
        if (staticData != null) {
            List<Map<String, Object>> staticScheduleDataList = (List<Map<String, Object>>)staticData;
            for (Map<String, Object> data : staticScheduleDataList) {
                Object contentId = data.get("contentId");
                staticMap.put(Long.valueOf(String.valueOf(contentId)), data);
            }
        }
        ItemEntityVO itemEntityVO = process.getValue();
        if (itemEntityVO != null) {
            itemEntityVO.put("solutionName", "gshItemRecommend");
            itemEntityVO.put("currentResourceId", String.valueOf(aldCurrentResId));
            Long itemId = itemEntityVO.getItemId();
            Map<String, Object> stringObjectMap = staticMap.get(itemId);
            if (stringObjectMap != null) {
                for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
                    itemEntityVO.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return process;
    }
}
