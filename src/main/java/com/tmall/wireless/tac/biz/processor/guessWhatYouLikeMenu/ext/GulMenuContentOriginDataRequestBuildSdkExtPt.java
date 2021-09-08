package com.tmall.wireless.tac.biz.processor.guessWhatYouLikeMenu.ext;

import com.ali.com.google.common.collect.Maps;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;

import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.guessWhatYouLikeMenu.constant.ConstantValue;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;

/**
 * @author Yushan
 * @date 2021/8/31 4:22 下午
*/
@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.B2C_CNXH_MENU_FEEDS
)
@Service
public class GulMenuContentOriginDataRequestBuildSdkExtPt extends Register implements ContentOriginDataRequestBuildSdkExtPt {
    @Resource
    TacLogger logger;

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {
        RecommendRequest tppRequest = new RecommendRequest();
        tppRequest.setLogResult(true);
        tppRequest.setAppId(ConstantValue.APPID);
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getUserDO)
                .map(UserDO::getUserId)
                .orElse(0L));

        Map<String, String> params = Maps.newHashMap();
        String regionCode = Joiner.on(",").join(Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getLocParams)
                .map(LocParams::getLogicIdByPriority)
                .orElse(Lists.newArrayList()));
        if (StringUtils.isEmpty(regionCode)) {
            regionCode = "107";
        }
        params.put("regionCode", regionCode);
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getLocParams)
                .map(LocParams::getSmAreaId)
                .orElse(0L)
                .toString());
        Integer index = Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getUserPageInfo)
                .map(PageInfoDO::getIndex)
                .orElse(0);
        params.put("isFirstPage", index > 0 ? "false" : "true");
        params.put("pageSize", Optional.ofNullable(sgFrameworkContextContent)
                .map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getUserPageInfo)
                .map(PageInfoDO::getPageSize)
                .orElse(8)
                .toString());
        Map<String, Object> map = Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getRequestParams).orElse(Maps.newHashMap());

        tppRequest.setParams(params);
        return tppRequest;
    }
}
