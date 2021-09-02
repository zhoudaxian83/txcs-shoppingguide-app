package com.tmall.wireless.tac.biz.processor.huichang.inventory.InventoryEntranceModule;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.vo.ContentFilterSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 过滤掉 售罄和不卖的商品，进而过滤掉没挂品的场景
 */
@SdkExtension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO_INVENTORY_ENTRANCE_MODULE)
public class InventoryEntranceModuleContentFilterSdkExtPt extends Register implements ContentFilterSdkExtPt {

    Logger logger = LoggerFactory.getLogger(InventoryEntranceModuleContentFilterSdkExtPt.class);

    @Override
    public SgFrameworkResponse<ContentVO> process(SgFrameworkContextContent sgFrameworkContextContent) {
        SgFrameworkResponse<ContentVO> contentVOSgFrameworkResponse = sgFrameworkContextContent
            .getContentVOSgFrameworkResponse();
        logger.info("-----------InventoryEntranceModuleContentFilterSdkExtPt.start-------");
        logger.info("-----------InventoryEntranceModuleContentFilterSdkExtPt.getItemAndContentList:{}", contentVOSgFrameworkResponse.getItemAndContentList());
        return sgFrameworkContextContent.getContentVOSgFrameworkResponse();
    }

}
