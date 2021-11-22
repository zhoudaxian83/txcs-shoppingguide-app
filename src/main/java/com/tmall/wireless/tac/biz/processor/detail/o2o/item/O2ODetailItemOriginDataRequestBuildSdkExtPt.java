package com.tmall.wireless.tac.biz.processor.detail.o2o.item;


import com.alibaba.metrics.StringUtils;

import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.common.config.DetailSwitch;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.TppParmasConstant;
import com.tmall.wireless.tac.biz.processor.detail.common.extabstract.AbstractDetailOriginDataRequestBuildSdkExtPt;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.model.config.DetailRequestConfig;
import com.tmall.wireless.tac.biz.processor.detail.util.CommonUtil;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.LocTypeEnum;

/**
 * @author: guichen
 * @Data: 2021/9/13
 * @Description:
 */
@SdkExtension(bizId = DetailConstant.BIZ_ID,
    useCase = DetailConstant.USE_CASE_O2O,
    scenario = DetailConstant.ITEM_SCENERIO)
public class O2ODetailItemOriginDataRequestBuildSdkExtPt extends AbstractDetailOriginDataRequestBuildSdkExtPt
    implements ItemOriginDataRequestBuildSdkExtPt {

    @Override
    public Long getAppId(String recType, SgFrameworkContext sgFrameworkContextContent) {
        return DetailRequestConfig.parse(recType).getTppId();
    }

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        RecommendRequest process = super.processRequest(sgFrameworkContextItem);

        DetailRecommendRequest detailRequest = DetailRecommendRequest.getDetailRequest(
            sgFrameworkContextItem.getTacContext());

        if(LocTypeEnum.B2C.getType().equals(process.getParams().get(TppParmasConstant.STRATEGY_2_IRECAL_KEY))){
            HadesLogUtil.stream(DetailConstant.ITEM_SCENERIO)
                .kv("appId",String.valueOf(process.getAppId()))
                .kv("userId", String.valueOf(process.getUserId()))
                .kv("locType",detailRequest.getLocType())
                .kv("csa", detailRequest.getCsa())
                .error();
        }

        if (CommonUtil.validId(detailRequest.getContentId())) {
            if (StringUtils.isNotBlank(detailRequest.getItemSetIds())) {
                process.getParams().put("itemSetIdList", detailRequest.getItemSetIds());
            }
        }

        return process;
    }
}
