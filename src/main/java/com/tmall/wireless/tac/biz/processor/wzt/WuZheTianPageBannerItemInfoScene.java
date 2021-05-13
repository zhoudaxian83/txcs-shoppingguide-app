package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.Optional;

import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MetaInfoUtil;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceItem;
import com.tmall.txcs.gs.model.biz.context.EntitySetParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.PmtParams;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by yangqing.byq on 2021/4/6.
 */
@Service
public class WuZheTianPageBannerItemInfoScene {



    Logger LOGGER = LoggerFactory.getLogger(WuZheTianPageBannerItemInfoScene.class);

    @Autowired
    SgFrameworkServiceItem sgFrameworkServiceItem;

    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> recommend(Context context) {

        Long level1Id = MapUtil.getLongWithDefault(context.getParams(), "level1Id", 0L);
        Long index = MapUtil.getLongWithDefault(context.getParams(), "index", 0L);
        Long pageSize = MapUtil.getLongWithDefault(context.getParams(), "pageSize", 20L);
        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L);


        LOGGER.error("ITEM_REQUEST:{}", JSON.toJSONString(context));

        context.getParams();

        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();

        sgFrameworkContextItem.setRequestParams(context.getParams());

        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setBiz(ScenarioConstant.BIZ_TYPE_SUPERMARKET);
        sceneInfo.setSubBiz(ScenarioConstant.LOC_TYPE_B2C);
        sceneInfo.setScene(ScenarioConstant.SCENARIO_GUL_SUB_TAB);
        sgFrameworkContextItem.setSceneInfo(sceneInfo);

        UserDO userDO = new UserDO();
        userDO.setUserId(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getUserId).orElse(0L));
        userDO.setNick(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getNick).orElse(""));
        sgFrameworkContextItem.setUserDO(userDO);

        sgFrameworkContextItem.setLocParams(CsaUtil.parseCsaObj(context.get(UserParamsKeyConstant.USER_PARAMS_KEY_CSA),smAreaId));
        sgFrameworkContextItem.setItemMetaInfo(MetaInfoUtil.getGulSubTabItemMetaInfo());

        EntitySetParams entitySetParams = new EntitySetParams();
        entitySetParams.setItemSetSource("crm");
        entitySetParams.setItemSetIdList(Lists.newArrayList(5233L));
        sgFrameworkContextItem.setEntitySetParams(entitySetParams);

        PmtParams pmtParams = new PmtParams();
        pmtParams.setPmtSource("sm_manager");
        pmtParams.setPmtName("guessULike");
        pmtParams.setPageId("cainixihuan1");
        pmtParams.setModuleId(level1Id.toString());
        sgFrameworkContextItem.setPmtParams(pmtParams);

        PageInfoDO pageInfoDO = new PageInfoDO();
        pageInfoDO.setIndex(index.intValue());
        pageInfoDO.setPageSize(pageSize.intValue());
        sgFrameworkContextItem.setUserPageInfo(pageInfoDO);

        return sgFrameworkServiceItem.recommend(sgFrameworkContextItem)
            .map(TacResult::newResult)
            .onErrorReturn(r -> TacResult.errorResult(""));

    }
}
