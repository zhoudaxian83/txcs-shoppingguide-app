package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.model.meta.ItemGroupMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemMetaInfo;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceItem;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.common.util.AldUrlParamUtil;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.common.ContentInfoSupport;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.PressureTestUtil;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FirstScreenMindItemScene4Ald extends FirstScreenMindItemScene {

    @Autowired
    TacLogger tacLogger;
    @Autowired
    SgFrameworkServiceItem sgFrameworkServiceItem;

    @Autowired
    ContentInfoSupport contentInfoSupport;

    public Flowable<TacResult<List<GeneralItem>>> recommend4Ald(RequestContext4Ald requestContext4Ald) {
        Long smAreaId = MapUtil.getLongWithDefault(requestContext4Ald.getAldParam(), "smAreaId", 330100L);

        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();

        sgFrameworkContextItem.setRequestParams(AldUrlParamUtil.getAldUrlKv(requestContext4Ald));

        sgFrameworkContextItem.setSceneInfo(getSceneInfo());
        sgFrameworkContextItem.setUserDO(getUserDO(requestContext4Ald.getUserInfo()));
        String csa = MapUtils.getString(requestContext4Ald.getAldParam(), UserParamsKeyConstant.USER_PARAMS_KEY_CSA);
        sgFrameworkContextItem.setLocParams(CsaUtil.parseCsaObj(csa, smAreaId));
        sgFrameworkContextItem.setItemMetaInfo(this.getRecommendItemMetaInfo());


        PageInfoDO pageInfoDO = new PageInfoDO();
        pageInfoDO.setIndex(0);
        pageInfoDO.setPageSize(20);
        sgFrameworkContextItem.setUserPageInfo(pageInfoDO);
        tacLogger.info("***FirstScreenMindItemScene sgFrameworkContextItem.toString()***:"+sgFrameworkContextItem.toString());

        return sgFrameworkServiceItem.recommend(sgFrameworkContextItem)
                .map(response -> {

                    Map<String, Object> contentInfo = queryContentInfo(sgFrameworkContextItem);

                    if (response.getExtInfos() == null) {
                        response.setExtInfos(Maps.newHashMap());
                    }
                    response.getExtInfos().put("contentModel", contentInfo);


                    List<GeneralItem> re = Lists.newArrayList();
                    re.add(convertAldItem(response));
                    return re;
                })
                .map(TacResult::newResult)
                .map(tacResult -> {
                    tacResult.getBackupMetaData().setUseBackup(true);
                    return tacResult;
                })
                .onErrorReturn(r -> TacResult.errorResult(""));

    }

    private GeneralItem convertAldItem(SgFrameworkResponse<EntityVO> response) {
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

    private UserDO getUserDO(UserInfo userInfo) {
        UserDO userDO = new UserDO();
        userDO.setUserId(userInfo.getUserId());
        userDO.setNick(userInfo.getNick());

        return userDO;
    }

}
