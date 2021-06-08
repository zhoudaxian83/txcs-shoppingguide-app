package com.tmall.wireless.tac.biz.processor.newproduct.ext;

import com.alibaba.cola.extension.Extension;
import com.google.common.collect.Maps;
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
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * @author haixiao.zhang
 * @date 2021/6/8
 */
@Extension(bizId = ScenarioConstant.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstant.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_SHANG_XIN_CONTENT)
@Service
public class SxlContentOriginDataRequestExtPt implements ContentOriginDataRequestExtPt {

    private static final Long APPID = 25831L;

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {

        /**
         * https://tui.taobao.com/recommend?appid=25831&itemSets=crm_5233&commerce=B2C&regionCode=108&smAreaId=330110&itemSetFilterTriggers=crm_5233&OPEN_MAINTENANCE=1
         */
        RecommendRequest tppRequest = new RecommendRequest();
        tppRequest.setAppId(APPID);

        sgFrameworkContextContent.getEntitySetParams().getContentSetIdList();
        Map<String, String> params = Maps.newHashMap();
        params.put("itemSets", "crm_322385,crm_5233");
        params.put("commerce", "B2C");
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getLocParams).map(LocParams::getSmAreaId).orElse(0L).toString());
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getUserDO)
            .map(UserDO::getUserId).orElse(0L));
        tppRequest.setParams(params);
        return tppRequest;
    }
}
