package com.tmall.wireless.tac.biz.processor.extremeItem;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigGroupList;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigs;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant.STATIC_SCHEDULE_DATA;


/**
 * Created from template by 言武 on 2021-09-10 14:36:48.
 *
 */

@Component
public class ExtremeItemSdkItemHandler extends TacReactiveHandler4Ald {

    @Autowired
    ShoppingguideSdkItemService shoppingguideSdkItemService;
    @Autowired
    TacLogger tacLogger;


    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald) throws Exception {
        try {
            tacLogger.info("context:" + JSON.toJSONString(requestContext4Ald));
            List<Map<String, Object>> aldDataList = (List<Map<String, Object>>) requestContext4Ald.getAldContext().get(STATIC_SCHEDULE_DATA);
            tacLogger.info("aldDataList:" + aldDataList);
            ItemConfigs itemConfigs = ItemConfigs.valueOf(aldDataList);
            tacLogger.info("itemConfigs:" + JSON.toJSONString(itemConfigs));
            itemConfigs.checkItemConfig();
            ItemConfigGroupList itemConfigGroupList = itemConfigs.splitGroup();
            tacLogger.info("itemConfigGroupList:" + JSON.toJSONString(itemConfigGroupList));
        } catch (Exception e) {
            tacLogger.error(e.getMessage(), e);
        }
        return Flowable.just(TacResult.newResult(new ArrayList<>()));
    }
}
