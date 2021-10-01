package com.tmall.wireless.tac.biz.processor.extremeItem;


import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import org.springframework.stereotype.Service;

/**
 * Created from template by 言武 on 2021-09-10 14:39:48.
 * 商品VO组装 - 商品VO组装.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "extremeItem"
)
public class ExtremeItemBuildItemVoSdkExtPt extends Register implements BuildItemVoSdkExtPt {
    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        return null;
    }
}
