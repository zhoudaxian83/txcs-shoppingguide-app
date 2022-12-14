package com.tmall.wireless.tac.biz.processor.huichang.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.meta.ItemGroupMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemMetaInfo;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceItem;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.todaycrazy.LimitTimeBuyScene;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.AldInfoUtil;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ??????????????????????????????
 * @author wangguohui
 */
@Service
public class HallCommonItemRequestProxy {

    Logger LOGGER = LoggerFactory.getLogger(LimitTimeBuyScene.class);
    @Autowired
    ShoppingguideSdkItemService shoppingguideSdkItemService;

    @Autowired
    AldInfoUtil aldInfoUtil;

    @Autowired
    TacLogger tacLogger;

    public Flowable<TacResult<List<GeneralItem>>> recommend(RequestContext4Ald requestContext4Ald, BizScenario bizScenario) {
        tacLogger.info("***huichang context.getParams()****:"+requestContext4Ald.getParams());
        LOGGER.info("***huichang context.getParams()****:"+requestContext4Ald.getParams());

        return shoppingguideSdkItemService.recommend(requestContext4Ald, bizScenario)
            .map(response -> {
                List<GeneralItem> re = Lists.newArrayList();
                re.add(convertAldItem(response));
                return re;
            })
            .map(TacResult::newResult)
            .onErrorReturn(r -> TacResult.errorResult(""));
    }

    public GeneralItem convertAldItem(SgFrameworkResponse<ItemEntityVO> response) {
        GeneralItem generalItem = new GeneralItem();
        generalItem.put("success", response.isSuccess());
        generalItem.put("errorCode", response.getErrorCode());
        generalItem.put("errorMsg", response.getErrorMsg());
        generalItem.put("itemAndContentList", response.getItemAndContentList());
        generalItem.put("extInfos", response.getExtInfos());
        generalItem.put("hasMore", response.isHasMore());
        generalItem.put("index", response.getIndex());

        return generalItem;
    }
}
