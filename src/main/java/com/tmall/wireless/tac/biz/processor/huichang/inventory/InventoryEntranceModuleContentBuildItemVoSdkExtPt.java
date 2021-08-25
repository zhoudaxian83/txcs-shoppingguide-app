package com.tmall.wireless.tac.biz.processor.huichang.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.tmall.tcls.gs.sdk.biz.extensions.content.vo.DefaultContentVoBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.content.vo.ContentVoBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.utils.URLUtil;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wangguohui
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_ENTRANCE_MODULE)
public class InventoryEntranceModuleContentBuildItemVoSdkExtPt
    extends DefaultContentVoBuildSdkExtPt implements ContentVoBuildSdkExtPt {
    @Autowired
    TacLogger tacLogger;

    @Override
    public SgFrameworkResponse<ContentVO> process(SgFrameworkContextContent sgFrameworkContextContent) {
        SgFrameworkResponse<ContentVO> contentVOSgFrameworkResponse = super.process(sgFrameworkContextContent);
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
                paramsMap.put("locType", locType);
                Object items = contentVO.get("items");
                if(items != null){
                    JSONArray jsonArray = JSON.parseArray(items.toString());
                    Object singleItem = jsonArray.get(0);
                    JSONObject jsonObject = JSON.parseObject(singleItem.toString());
                    String itemId = jsonObject.getString("itemId");
                    paramsMap.put("entryItemId", itemId);
                }
                String urlParamsByMap = URLUtil.getUrlParamsByMap(paramsMap);
                contentVO.put("urlParams", urlParamsByMap);
            }
            for(ContentVO contentVO : itemAndContentList){
                String urlParams = contentVO.getString("urlParams");
                contentVO.put("urlParams", urlParams + "&filterContentIds=" + String.join(",", filterContentIds));
            }
        }
        return contentVOSgFrameworkResponse;
    }


}
