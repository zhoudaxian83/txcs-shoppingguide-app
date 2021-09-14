package com.tmall.wireless.tac.biz.processor.detail.o2o.item;

import com.alibaba.cola.extension.Extension;

import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.common.config.DetailSwitch;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.biz.processor.detail.common.extabstract.AbstractDetailOriginDataRequestBuildSdkExtPt;
import org.springframework.stereotype.Service;

/**
 * @author: guichen
 * @Data: 2021/9/13
 * @Description:
 */
@Extension(bizId = DetailConstant.BIZ_ID,
    useCase = DetailConstant.USE_CASE_O2O,
    scenario = DetailConstant.ITEM_SCENERIO)
@Service
public class O2ODetailItemOriginDataRequestBuildSdkExtPt extends AbstractDetailOriginDataRequestBuildSdkExtPt
    implements ItemOriginDataRequestBuildSdkExtPt {

    @Override
    public Long getAppId(String recType) {
        return DetailSwitch.appIdMap.get(recType);
    }

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        return super.process(sgFrameworkContextItem);
    }
}
