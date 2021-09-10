package com.tmall.wireless.tac.biz.processor.detail.o2o.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Resource;

import com.alibaba.cola.extension.Extension;

import com.taobao.usa.util.StringUtils;
import com.tmall.aselfcommon.constant.LocType;
import com.tmall.aselfcommon.lbs.service.LocationReadService;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.CommonUserParams;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.tcls.gs.sdk.framework.model.context.UserDO;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.TppItemBusinessType;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.TppParmasConstant;
import com.tmall.wireless.tac.biz.processor.detail.common.extabstract.DetailCommonContentOriginDataRequestBuildSdkExtPt;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.util.CommonUtil;
import org.springframework.stereotype.Service;

/**
 * @author: guichen
 * @Data: 2021/9/10
 * @Description:
 */
@Extension(bizId = DetailConstant.BIZ_ID,
    useCase = DetailConstant.USE_CASE_O2O,
    scenario = DetailConstant.CONTENT_SCENERIO)
@Service
public class O2ODetailContentOriginDataRequestBuildSdkExtPt extends DetailCommonContentOriginDataRequestBuildSdkExtPt {

    @Override
    public Long getAppId(){
        return 21174L;
    }

}
