package com.tmall.wireless.tac.biz.processor.huichang.hotitem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.service.HallCommonItemRequestProxy;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class HotItemModuleHandler extends TacReactiveHandler4Ald {

    Logger logger = LoggerFactory.getLogger(HotItemModuleHandler.class);

    private static final String defaultLocType = "B2C";

    @Autowired
    TacLogger tacLogger;

    @Autowired
    ShoppingguideSdkItemService shoppingguideSdkItemService;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {

        BizScenario bizScenario = BizScenario.valueOf(HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
            HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
            HallScenarioConstant.HALL_SCENARIO_HOT_ITEM);
        bizScenario.addProducePackage(HallScenarioConstant.HALL_ITEM_SDK_PACKAGE);

        Flowable<TacResult<List<GeneralItem>>> tacResultFlowable = shoppingguideSdkItemService.recommend(
            requestContext4Ald, bizScenario)
            .map(response -> {
                List<GeneralItem> re = Lists.newArrayList();
                re.add(convertAldItem(response));
                return re;
            })
            .map(TacResult::newResult)
            .onErrorReturn(r -> TacResult.errorResult(""));
        return tacResultFlowable;


        //Map<String, Object> aldContext = requestContext4Ald.getAldContext();
        //Map<String, Object> aldParam = requestContext4Ald.getAldParam();
        //Object aldStaticData  = aldContext.get(HallCommonAldConstant.STATIC_SCHEDULE_DATA);
        //if(aldStaticData == null){
        //    throw new Exception("静态数据获取为空");
        //}
        //List<Map<String, Object>> aldStaticDataList = (List<Map<String, Object>>)aldStaticData;
        //String locType = defaultLocType;
        //String tacParams = MapUtil.getStringWithDefault(aldParam, "tacParams", "");
        //if(StringUtils.isNotBlank(tacParams)){
        //    JSONObject tacParamsMap = JSON.parseObject(tacParams);
        //    locType = Optional.ofNullable(tacParamsMap.getString(HallCommonAldConstant.LOC_TYPE)).orElse(defaultLocType);
        //}
        //
        //
        //return null;
    }

    public GeneralItem convertAldItem(SgFrameworkResponse<ItemEntityVO> response) {
        GeneralItem generalItem = new GeneralItem();
        generalItem.put("data", response.getItemAndContentList());
        return generalItem;
    }

}
