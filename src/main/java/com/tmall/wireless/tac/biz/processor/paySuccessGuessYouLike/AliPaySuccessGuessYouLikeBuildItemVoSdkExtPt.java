package com.tmall.wireless.tac.biz.processor.paySuccessGuessYouLike;

import com.alibaba.fastjson.JSON;
import com.tmall.tcls.gs.sdk.biz.extensions.item.vo.DefaultBuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;

public class AliPaySuccessGuessYouLikeBuildItemVoSdkExtPt  extends DefaultBuildItemVoSdkExtPt
        implements BuildItemVoSdkExtPt {

    @Autowired
    TacLoggerImpl tacLogger;
    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        tacLogger.info("构建商品vo前信息：" + JSON.toJSONString(buildItemVoRequest));
        Response<ItemEntityVO> result = super.process(buildItemVoRequest);
        tacLogger.info("构建商品vo后信息：" + JSON.toJSONString(result));
        return result;
    }
}
