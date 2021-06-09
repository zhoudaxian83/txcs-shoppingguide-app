package com.tmall.wireless.tac.biz.processor.newproduct.ext;

import com.alibaba.aladdin.lamp.domain.request.Request;
import com.alibaba.aladdin.lamp.domain.request.RequestItem;
import com.alibaba.aladdin.lamp.domain.request.modules.DeviceInfo;
import com.alibaba.aladdin.lamp.domain.request.modules.LocationInfo;
import com.alibaba.aladdin.lamp.domain.response.ResResponse;
import com.alibaba.aladdin.lamp.domain.user.UserProfile;
import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ContentOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.txcs.gs.spi.recommend.AldSpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.newproduct.handler.SxlItemFeedsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * tpp入参组装扩展点
 * @author haixiao.zhang
 * @date 2021/6/8
 */
@Extension(bizId = ScenarioConstant.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstant.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_SHANG_XIN_CONTENT)
@Service
public class SxlContentOriginDataRequestExtPt implements ContentOriginDataRequestExtPt {

    Logger LOGGER = LoggerFactory.getLogger(SxlContentOriginDataRequestExtPt.class);

    private static final Long APPID = 25831L;

    @Autowired
    private AldSpi aldSpi;

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {

        Map<String, ResResponse> aldResponse = getAldInfo(sgFrameworkContextContent);
        LOGGER.error("SxlContentOriginDataRequestExtPt aldResponse:{}",JSON.toJSONString(aldResponse));
        /**
         * https://tui.taobao.com/recommend?appid=25831&itemSets=crm_5233&commerce=B2C&regionCode=108&smAreaId=330110&itemSetFilterTriggers=crm_5233&OPEN_MAINTENANCE=1
         */
        RecommendRequest tppRequest = new RecommendRequest();
        tppRequest.setAppId(APPID);
        Map<String, String> params = Maps.newHashMap();
        params.put("itemSets", "crm_322385,crm_5233");
        params.put("commerce", "B2C");
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getLocParams).map(LocParams::getSmAreaId).orElse(0L).toString());
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getUserDO)
            .map(UserDO::getUserId).orElse(0L));
        tppRequest.setParams(params);
        return tppRequest;
    }


    private Map<String, ResResponse> getAldInfo(SgFrameworkContextContent sgFrameworkContextContent){

       return aldSpi.queryAldInfoSync(buildAldRequest(sgFrameworkContextContent));

    }

    private Request buildAldRequest(SgFrameworkContextContent sgFrameworkContextContent){
        Request request = new Request();
        request.setBizId(101);
        request.setCallSource("txcs-shoppingguide");
        request.setDebug(false);
        RequestItem item = new RequestItem();
        item.setResId("17390113");
        UserProfile userProfile = request.getUserProfile();
        userProfile.setUserId(sgFrameworkContextContent.getUserDO().getUserId());
        DeviceInfo deviceInfo = request.getDeviceInfo();;
        deviceInfo.setTtid(sgFrameworkContextContent.getUserDO().getUtdid());
        request.setRequestItems(Lists.newArrayList(item));
        //地址信息
        LocationInfo locationInfo = request.getLocationInfo();
        //四级地址
        locationInfo.setCityLevel4(String.valueOf(sgFrameworkContextContent.getLocParams().getSmAreaId()));
        List<String> wdkCodes = Lists.newArrayList();
        locationInfo.setWdkCodes(wdkCodes);
        return request;

    }
}
