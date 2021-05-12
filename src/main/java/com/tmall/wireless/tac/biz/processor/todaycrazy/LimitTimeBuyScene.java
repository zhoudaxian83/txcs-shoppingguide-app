package com.tmall.wireless.tac.biz.processor.todaycrazy;

import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceItem;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

public class LimitTimeBuyScene {

    @Autowired
    SgFrameworkServiceItem sgFrameworkServiceItem;

    @Autowired
    TacLogger tacLogger;

    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> recommend(Context context) {
        tacLogger.info("***LimitTimeBuyScene context****:"+context);

        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L);
        String bannerInfo = MapUtil.getStringWithDefault(
                context.getParams(),
                RequestKeyConstantApp.BANNER_INFO,
                "");


        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();

        sgFrameworkContextItem.setRequestParams(context.getParams());

        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setBiz(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET);
        sceneInfo.setSubBiz(ScenarioConstantApp.LOC_TYPE_B2C);
        sceneInfo.setScene(ScenarioConstantApp.SCENARIO_FIRST_PAGE_BANNER_ITEM);
        sgFrameworkContextItem.setSceneInfo(sceneInfo);

        UserDO userDO = new UserDO();
        userDO.setUserId(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getUserId).orElse(0L));
        userDO.setNick(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getNick).orElse(""));
        sgFrameworkContextItem.setUserDO(userDO);

        sgFrameworkContextItem.setLocParams(CsaUtil.parseCsaObj(context.get(UserParamsKeyConstant.USER_PARAMS_KEY_CSA), smAreaId));
        //sgFrameworkContextItem.setItemMetaInfo(getBannerItemMetaInfo());


        PageInfoDO pageInfoDO = new PageInfoDO();
        pageInfoDO.setIndex(0);
        pageInfoDO.setPageSize(20);
        sgFrameworkContextItem.setUserPageInfo(pageInfoDO);

        return sgFrameworkServiceItem.recommend(sgFrameworkContextItem)
                .map(TacResult::newResult)
                .onErrorReturn(r -> TacResult.errorResult(""));

    }
}
