package com.tmall.wireless.tac.biz.processor.huichang.inventory.InventoryEntranceModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.tmall.aselfcaptain.util.StackTraceUtil;
import com.tmall.tcls.gs.sdk.biz.extensions.content.vo.DefaultContentVoBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.content.vo.ContentVoBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.PageUrlUtil;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.URLUtil;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wangguohui
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_ENTRANCE_MODULE)
public class InventoryEntranceModuleContentBuildItemVoSdkExtPt
    extends DefaultContentVoBuildSdkExtPt implements ContentVoBuildSdkExtPt {

    Logger logger = LoggerFactory.getLogger(InventoryEntranceModuleContentBuildItemVoSdkExtPt.class);

    @Override
    public SgFrameworkResponse<ContentVO> process(SgFrameworkContextContent sgFrameworkContextContent) {
        logger.info("-------InventoryEntranceModuleContentBuildItemVoSdkExtPt.start.---------");

        SgFrameworkResponse<ContentVO> response = new SgFrameworkResponse();
        try{
            SgFrameworkResponse<ContentVO> contentVOSgFrameworkResponse = super.process(sgFrameworkContextContent);
            logger.info("InventoryEntranceModuleContentBuildItemVoSdkExtPt.super.process.getItemAndContentList:{}",JSON.toJSONString(contentVOSgFrameworkResponse.getItemAndContentList()));
            Context tacContext = sgFrameworkContextContent.getTacContext();
            RequestContext4Ald requestContext4Ald = (RequestContext4Ald)tacContext;
            Map<String, Object> aldContext = requestContext4Ald.getAldContext();
            Map<String, Object> aldParam = requestContext4Ald.getAldParam();
            Object aldCurrentResId = aldContext.get(HallCommonAldConstant.ALD_CURRENT_RES_ID);
            String entryResourceId = PageUrlUtil.getParamFromCurPageUrl(aldParam, "entryResourceId");

            Map<String, Object> userParams = sgFrameworkContextContent.getUserParams();
            String locType = MapUtil.getStringWithDefault(userParams, "locType", "B2C");
            List<ContentVO> itemAndContentList = contentVOSgFrameworkResponse.getItemAndContentList();
            List<String> filterContentIds = new ArrayList<>();//入口一排三  需要把入口一排三的每个清单透出的每个场景带到下一页
            if(CollectionUtils.isNotEmpty(itemAndContentList)){
                for (int i = 0; i < itemAndContentList.size(); i++) {
                    ContentVO contentVO = itemAndContentList.get(i);
                    Map<String, String> paramsMap = new HashMap<>();
                    String contentId = contentVO.getString("contentId");
                    paramsMap.put("entryContentIds", contentId);
                    //入口只展示三个
                    if(i < 3){
                        filterContentIds.add(contentId);
                    }
                    String contentSetId = contentVO.getString("contentSetId");
                    paramsMap.put("contentSetId", contentSetId);

                    String contentSetTitle = contentVO.getString("contentSetTitle");
                    paramsMap.put("contentSetTitle", contentSetTitle);

                    String contentSetSubTitle = contentVO.getString("contentSetSubTitle");
                    paramsMap.put("contentSetSubTitle", contentSetSubTitle);

                    paramsMap.put("locType", locType);
                    Object items = contentVO.get("items");
                    if(items != null){
                        JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(items));
                        Object singleItem = jsonArray.get(0);
                        JSONObject jsonObject = JSON.parseObject(singleItem.toString());
                        String itemId = jsonObject.getString("itemId");
                        paramsMap.put("entryItemId", itemId);
                    }
                    //绑定资源位id
                    if(StringUtils.isNotEmpty(entryResourceId)){
                        paramsMap.put("entryResourceId", entryResourceId);
                    }else {
                        paramsMap.put("entryResourceId", String.valueOf(aldCurrentResId));
                    }
                    String urlParamsByMap = URLUtil.getUrlParamsByMap(paramsMap);
                    contentVO.put("urlParams", urlParamsByMap);
                }
                for(ContentVO contentVO : itemAndContentList){
                    String urlParams = contentVO.getString("urlParams");
                    contentVO.put("urlParams", urlParams + "&filterContentIds=" + String.join(",", filterContentIds));
                }
            }
            logger.info("InventoryEntranceModuleContentBuildItemVoSdkExtPt.result:{}", JSON.toJSONString(contentVOSgFrameworkResponse.getItemAndContentList()));
            return contentVOSgFrameworkResponse;
        }catch (Exception e){
            logger.error("InventoryEntranceModuleContentBuildItemVoSdkExtPt.start.error:{}", StackTraceUtil.stackTrace(e));
            response.setErrorMsg("InventoryEntranceModuleContentBuildItemVoSdkExtPt.error");
            return response;
        }

    }


}
