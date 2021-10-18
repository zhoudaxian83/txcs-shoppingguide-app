package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab;

import com.tmall.tcls.gs.sdk.biz.uti.UserCommonParamsUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextbuild.ItemUserCommonParamsBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.CommonUserParams;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.domain.Context;

/**
 * Created from template by 罗俊冲 on 2021-10-14 23:13:46.
 */

@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB
)
public class TodayCrazyRecommendTabItemUserCommonParamsBuildSdkExtPt extends Register implements ItemUserCommonParamsBuildSdkExtPt {
    @Override
    public CommonUserParams process(Context context) {

        return UserCommonParamsUtil.process(context);
    }
}
