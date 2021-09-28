package com.tmall.wireless.tac.biz.processor.huichang.hotitem;

import java.util.Map;

import com.alibaba.fastjson.JSON;

import com.tmall.tcls.gs.sdk.biz.extensions.item.vo.DefaultBuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemInfoDTO;
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
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_HOT_ITEM)
public class HotItemBuildItemVoSdkExtPt extends DefaultBuildItemVoSdkExtPt implements BuildItemVoSdkExtPt {

    Logger logger = LoggerFactory.getLogger(HotItemBuildItemVoSdkExtPt.class);

    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        Response<ItemEntityVO> process = super.process(buildItemVoRequest);
        ItemInfoDTO itemInfoDTO = buildItemVoRequest.getItemInfoDTO();
        logger.error("HotItemBuildItemVoSdkExtPt.itemInfoDTO:{}", JSON.toJSONString(itemInfoDTO));
        ItemEntityVO itemEntityVO = process.getValue();
        SgFrameworkContextItem sgFrameworkContextItem = buildItemVoRequest.getContext();
        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)(sgFrameworkContextItem.getTacContext());

        Map<String, Object> aldContext = requestContext4Ald.getAldContext();
        Object aldStaticData = aldContext.get(HallCommonAldConstant.STATIC_SCHEDULE_DATA);
        if(null != aldStaticData){
            Object aldStaticDataMap = requestContext4Ald.getParams().get("aldStaticDataMap");
            Map<Long, Map<String, Object>> staticDataMap = (Map<Long, Map<String, Object>>)aldStaticDataMap;
            Long itemId = itemEntityVO.getItemId();
            Map<String, Object> itemDataMap = staticDataMap.get(itemId);
            if(itemDataMap != null){
                Object itemImg = itemDataMap.get("itemImg");
                if(itemImg != null){
                    itemEntityVO.put("itemImg", String.valueOf(itemImg));
                }
                Object shortTitle = itemDataMap.get("shortTitle");
                if(shortTitle != null){
                    itemEntityVO.put("shortTitle", String.valueOf(shortTitle));
                }
                Object itemDesc = itemDataMap.get("itemDesc");
                if(itemDesc != null){
                    itemEntityVO.put("itemDesc", String.valueOf(itemDesc));
                }
                Object industryId = itemDataMap.get("industryId");
                if(industryId != null){
                    itemEntityVO.put("industryId", String.valueOf(industryId));
                }
            }
        }
        return process;
    }
}
