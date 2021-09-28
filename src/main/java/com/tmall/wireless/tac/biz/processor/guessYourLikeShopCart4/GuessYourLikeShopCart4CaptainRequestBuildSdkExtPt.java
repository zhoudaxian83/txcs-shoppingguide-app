package com.tmall.wireless.tac.biz.processor.guessYourLikeShopCart4;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.iteminfo.CaptainRequestBuildRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.iteminfo.CaptainRequestBuildSdkExtPt;
import com.tmall.wireless.store.spi.render.model.RenderRequest;
import org.springframework.stereotype.Service;

/**
 * Created from template by 程斐斐 on 2021-09-28 16:45:35.
 * captain请求组装 - captain请求组装.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "guessYourLikeShopCart4"
)
public class GuessYourLikeShopCart4CaptainRequestBuildSdkExtPt extends Register implements CaptainRequestBuildSdkExtPt {
    @Override
    public RenderRequest process(CaptainRequestBuildRequest captainRequestBuildRequest) {
        return null;
    }
}
