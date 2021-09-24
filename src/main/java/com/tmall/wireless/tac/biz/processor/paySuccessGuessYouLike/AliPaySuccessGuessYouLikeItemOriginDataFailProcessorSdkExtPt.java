package com.tmall.wireless.tac.biz.processor.paySuccessGuessYouLike;

import com.tmall.tcls.gs.sdk.biz.extensions.item.origindata.DefaultItemOriginDataFailProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataFailProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.springframework.beans.factory.annotation.Autowired;

public class AliPaySuccessGuessYouLikeItemOriginDataFailProcessorSdkExtPt extends
        DefaultItemOriginDataFailProcessorSdkExtPt implements ItemOriginDataFailProcessorSdkExtPt {


    @Autowired
    TacLoggerImpl tacLogger;
    @Override
    public OriginDataDTO<ItemEntity> process(OriginDataProcessRequest originDataProcessRequest) {

        tacLogger.info("进入元数据失败处理扩展点");
        OriginDataDTO<ItemEntity> process = super.process(originDataProcessRequest);
        tacLogger.info("进入元数据失败处理扩展点");
        return process;
    }
}
