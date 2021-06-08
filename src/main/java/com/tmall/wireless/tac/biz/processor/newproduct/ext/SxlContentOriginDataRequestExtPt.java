package com.tmall.wireless.tac.biz.processor.newproduct.ext;

import com.alibaba.cola.extension.Extension;
import com.google.common.collect.Maps;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
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
public class SxlContentOriginDataRequestExtPt implements ItemOriginDataRequestExtPt {

    private static final Long APPID = 24910L;

    private static final String pageSize = "20";


    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {

        /**
         * https://tui.taobao.com/recommend?appid=24910&itemSets=crm_219953,crm_219840&pageSize=20&index=0
         */
        RecommendRequest tppRequest = new RecommendRequest();
        tppRequest.setAppId(APPID);

        sgFrameworkContextItem.getEntitySetParams().getContentSetIdList();
        Map<String, String> params = Maps.newHashMap();
        params.put("pageSize", pageSize);
        params.put("itemSets", "crm_322385");

        Integer index = Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getUserPageInfo).map(
            PageInfoDO::getIndex).orElse(0);
        params.put("index", "0");
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getUserDO)
            .map(UserDO::getUserId).orElse(0L));
        tppRequest.setParams(params);
        return tppRequest;
    }
}
