package com.tmall.wireless.tac.biz.processor.guessYourLikeShopCart4;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextbuild.ItemMetaInfoBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.meta.ItemMetaInfo;
import com.tmall.wireless.tac.client.domain.Context;
import org.springframework.stereotype.Service;

/**
 * Created from template by 程斐斐 on 2021-09-28 16:45:35.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "guessYourLikeShopCart4"
)
public class GuessYourLikeShopCart4ItemMetaInfoBuildSdkExtPt extends Register implements ItemMetaInfoBuildSdkExtPt {
    @Override
    public ItemMetaInfo process(Context context) {
        return null;
    }
}
