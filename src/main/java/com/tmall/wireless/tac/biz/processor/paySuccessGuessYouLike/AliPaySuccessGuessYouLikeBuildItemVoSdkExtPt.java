package com.tmall.wireless.tac.biz.processor.paySuccessGuessYouLike;

import com.alibaba.fastjson.JSON;
import com.tmall.tcls.gs.sdk.biz.extensions.item.vo.DefaultBuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;


@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.PAY_FOR_SUCCESS_GUESS_YOU_LIKE
)
public class AliPaySuccessGuessYouLikeBuildItemVoSdkExtPt extends DefaultBuildItemVoSdkExtPt
        implements BuildItemVoSdkExtPt {


    @Autowired
    TacLoggerImpl tacLogger;

    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {


        tacLogger.info("vo解析前信息：" + JSON.toJSONString(buildItemVoRequest));
        Response<ItemEntityVO> process = super.process(buildItemVoRequest);

        tacLogger.info("vo解析后信息：" + JSON.toJSONString(process));
        return process;
    }


//    @Override
//    public SgFrameworkResponse<ContentVO> process(SgFrameworkContextContent sgFrameworkContextContent) {
//
//
//        tacLogger.info("vo解析前信息：" + JSON.toJSONString(sgFrameworkContextContent));
//        SgFrameworkResponse<ContentVO> process = super.process(sgFrameworkContextContent);
//        tacLogger.info("vo解析后信息：" + JSON.toJSONString(process));
//        return process;
//    }
}
