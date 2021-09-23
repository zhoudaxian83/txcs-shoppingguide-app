package com.tmall.wireless.tac.biz.processor.paySuccessGuessYouLike;

import com.tmall.tcls.gs.sdk.biz.extensions.item.vo.DefaultBuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.Response;

public class AliPaySuccessGuessYouLikeBuildItemVoSdkExtPt  extends DefaultBuildItemVoSdkExtPt
        implements BuildItemVoSdkExtPt {

    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        Response<ItemEntityVO> result = super.process(buildItemVoRequest);
        return null;
    }
}
