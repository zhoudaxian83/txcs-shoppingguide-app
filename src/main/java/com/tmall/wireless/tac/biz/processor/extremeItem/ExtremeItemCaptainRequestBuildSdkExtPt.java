package com.tmall.wireless.tac.biz.processor.extremeItem;

import com.alibaba.fastjson.JSON;
import com.tmall.aselfcaptain.item.model.ItemId;
import com.tmall.aselfcaptain.item.model.QueryOptionDO;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.iteminfo.CaptainRequestBuildRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.iteminfo.CaptainRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.captain.DefaultCaptainRequestBuildSdkExtPt;
import com.tmall.wireless.store.spi.render.model.RenderRequest;
import com.tmall.wireless.tac.biz.processor.extremeItem.domain.ItemConfigs;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant.STATIC_SCHEDULE_DATA;

/**
 * Created from template by 言武 on 2021-09-10 14:39:48.
 * captain请求组装 - captain请求组装.
 */

@SdkExtension(
        bizId = "hall",
        useCase = "B2C",
        scenario = "extreme_item"
)
public class ExtremeItemCaptainRequestBuildSdkExtPt extends DefaultCaptainRequestBuildSdkExtPt implements CaptainRequestBuildSdkExtPt {
    @Autowired
    TacLogger tacLogger;
    private final String captainSceneCode = "supermarket.hall.extremeItem";

    @Override
    public RenderRequest process(CaptainRequestBuildRequest captainRequestBuildRequest) {
        tacLogger.info("扩展点InventoryChannelItemPageCaptainRequestBuildSdkExtPt");
        RenderRequest renderRequest = super.process(captainRequestBuildRequest);
        QueryOptionDO option = renderRequest.getOption();
        option.setSceneCode(captainSceneCode);
        renderRequest.setOption(option);

        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)captainRequestBuildRequest.getContextItem().getTacContext();
        List<Map<String, Object>> aldDataList = (List<Map<String, Object>>) requestContext4Ald.getAldContext().get(STATIC_SCHEDULE_DATA);
        tacLogger.info("aldDataList:" + aldDataList);
        ItemConfigs itemConfigs = ItemConfigs.valueOf(aldDataList);
        tacLogger.info("itemConfigs:" + JSON.toJSONString(itemConfigs));
        itemConfigs.checkItemConfig();
        List<Long> itemIds = itemConfigs.extractItemIds();
        List<ItemId> itemIdList = itemIds.stream()
                .map(itemId -> ItemId.valueOf(itemId, ItemId.ItemType.B2C))
                .collect(Collectors.toList());
        renderRequest.getQuery().setItemIds(itemIdList);
        tacLogger.info("renderRequest:" + JSON.toJSONString(renderRequest));
        return renderRequest;
    }
}
