package com.tmall.wireless.tac.biz.processor.detail.common;

import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextbuild.ItemUserCommonParamsBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.CommonUserParams;
import com.tmall.wireless.tac.client.domain.Context;

/**
 * @author: guichen
 * @Data: 2021/9/14
 * @Description:详情没有csa的话，从tair取数据
 */
public class DetailCommonParamsBuildExtPt extends Register implements ItemUserCommonParamsBuildSdkExtPt  {
    @Override
    public CommonUserParams process(Context context) {
        return null;
    }
}
