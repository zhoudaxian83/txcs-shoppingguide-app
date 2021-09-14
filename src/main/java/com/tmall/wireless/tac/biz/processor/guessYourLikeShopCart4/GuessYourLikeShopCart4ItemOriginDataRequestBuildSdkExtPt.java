package com.tmall.wireless.tac.biz.processor.guessYourLikeShopCart4;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.ItemOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import org.springframework.stereotype.Service;

/**
 * Created from template by 程斐斐 on 2021-09-14 21:01:53.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "guessYourLikeShopCart4"
)
@Service
public class GuessYourLikeShopCart4ItemOriginDataRequestBuildSdkExtPt extends Register implements ItemOriginDataRequestBuildSdkExtPt {
    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        return null;
    }
}
