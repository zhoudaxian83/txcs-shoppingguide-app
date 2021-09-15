package com.tmall.wireless.tac.biz.processor.detail.o2o.item;

import java.util.Objects;

import com.alibaba.cola.extension.Extension;
import com.alibaba.metrics.StringUtils;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.common.config.DetailSwitch;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.biz.processor.detail.common.extabstract.AbstractDetailOriginDataRequestBuildSdkExtPt;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.util.CommonUtil;
import org.springframework.stereotype.Service;

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
        DetailRecommendRequest detailRequest = DetailRecommendRequest.getDetailRequest(
            sgFrameworkContextContent.getTacContext());
        //相似商品推荐的contentId
        if(!CommonUtil.validId(detailRequest.getContentId())){
            return 21174L;
        }
        return DetailSwitch.appIdMap.get(recType);
    }

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        RecommendRequest process = super.process(sgFrameworkContextItem);

        DetailRecommendRequest detailRequest = DetailRecommendRequest.getDetailRequest(
            sgFrameworkContextItem.getTacContext());
        if(CommonUtil.validId(detailRequest.getContentId())){
            if(StringUtils.isNotBlank(detailRequest.getItemSetIds())){
                process.getParams().put("itemSets",detailRequest.getItemSetIds());
            }
        }

        return process;
    }
}
