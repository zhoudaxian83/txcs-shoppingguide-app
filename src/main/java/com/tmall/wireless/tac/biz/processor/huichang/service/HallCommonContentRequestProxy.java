package com.tmall.wireless.tac.biz.processor.huichang.service;

import java.util.List;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkContentService;
import com.tmall.wireless.tac.biz.processor.todaycrazy.LimitTimeBuyScene;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.AldInfoUtil;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 会场通用参数组装服务
 * @author wangguohui
 */
@Service
public class HallCommonContentRequestProxy {


    Logger LOGGER = LoggerFactory.getLogger(LimitTimeBuyScene.class);

    @Autowired
    ShoppingguideSdkContentService shoppingguideSdkContentService;

    @Autowired
    AldInfoUtil aldInfoUtil;

    @Autowired
    TacLogger tacLogger;

    private static final String SceneCode = "superMarket_todayCrazy";

    public Flowable<TacResult<List<GeneralItem>>> recommend(RequestContext4Ald requestContext4Ald, BizScenario bizScenario) {
        return shoppingguideSdkContentService.recommend(requestContext4Ald, bizScenario)
            .map(response -> {
                List<GeneralItem> re = Lists.newArrayList();
                re.add(convertAldItem(response));
                return re;
            })
            .map(TacResult::newResult)
            .onErrorReturn(r -> TacResult.errorResult(""));
    }

    public GeneralItem convertAldItem(SgFrameworkResponse<ContentVO> response) {
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
