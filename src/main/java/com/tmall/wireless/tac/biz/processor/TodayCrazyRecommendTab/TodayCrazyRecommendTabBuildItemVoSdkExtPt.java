package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;


import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import org.springframework.stereotype.Service;

/**
 * Created from template by 罗俊冲 on 2021-09-24 14:09:23.
 * 商品VO组装 - 商品VO组装.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "TodayCrazyRecommendTab"
)
public class TodayCrazyRecommendTabBuildItemVoSdkExtPt extends Register implements BuildItemVoSdkExtPt {
    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        return null;
    }
}
