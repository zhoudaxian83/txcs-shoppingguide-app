package com.tmall.wireless.tac.biz.processor.huichang.inventory.inventoryChannelItemPage;

import com.tmall.aselfcaptain.item.model.QueryOptionDO;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.iteminfo.CaptainRequestBuildRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.iteminfo.CaptainRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.sm.iteminfo.bysource.captain.DefaultCaptainRequestBuildSdkExtPt;
import com.tmall.wireless.store.spi.render.model.RenderRequest;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 修改captain的sceneCode，获得卖点数据
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
        useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
        scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_CHANNEL_ITEM_PAGE)
public class InventoryChannelItemPageCaptainRequestBuildSdkExtPt extends DefaultCaptainRequestBuildSdkExtPt implements CaptainRequestBuildSdkExtPt {
    @Autowired
    TacLogger tacLogger;
    private final String captainSceneCode = "supermarket.hall.inventory";

    @Override
    public RenderRequest process(CaptainRequestBuildRequest captainRequestBuildRequest) {
        tacLogger.debug("扩展点InventoryChannelItemPageCaptainRequestBuildSdkExtPt");
        RenderRequest renderRequest = super.process(captainRequestBuildRequest);
        QueryOptionDO option = renderRequest.getOption();
        option.setSceneCode(captainSceneCode);
        renderRequest.setOption(option);
        return renderRequest;
    }
}
