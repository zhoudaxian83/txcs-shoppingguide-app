package com.tmall.wireless.tac.biz.processor.newproduct.ext;

import com.alibaba.cola.extension.Extension;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
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
 * @date 2021/6/7
 */
@Extension(bizId = ScenarioConstant.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstant.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
@Service
public class SxlItemOriginDataRequestExtPt implements ItemOriginDataRequestExtPt {

    private static final Long APPID = 24910L;

    private static final int pageSize = 20;


    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {

        /**
         * https://tui.taobao.com/recommend?appid=24910&itemSets=crm_219953,crm_219840&pageSize=20&index=0
         */
        RecommendRequest tppRequest = new RecommendRequest();
        tppRequest.setAppId(APPID);

        sgFrameworkContextItem.getEntitySetParams().getContentSetIdList();
        Map<String, Object> params = Maps.newHashMap();
        params.put("pageSize", pageSize);
        params.put("itemSets", "crm_32285");

        Integer index = Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getUserPageInfo).map(
            PageInfoDO::getIndex).orElse(0);
        params.put("index", index);
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getUserDO)
            .map(UserDO::getUserId).orElse(0L));

        return tppRequest;
    }
}
