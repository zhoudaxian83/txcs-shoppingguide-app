package com.tmall.wireless.tac.biz.processor.youbaozang;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.framework.model.meta.ItemGroupMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemRecommendMetaInfo;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceItem;
import com.tmall.txcs.gs.model.biz.context.EntitySetParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Created by yangqing.byq on 2021/3/9.
 */
@Component
public class YouBaoZangHandler extends RpmReactiveHandler<SgFrameworkResponse<EntityVO>> {
    @Autowired
    SgFrameworkServiceItem sgFrameworkServiceItem;
    @Override
    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> executeFlowable(Context context) throws Exception {

        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), RequestKeyConstantApp.SMAREAID, 330100L);
        Long itemSetId = MapUtil.getLongWithDefault(context.getParams(), RequestKeyConstantApp.ITEMSET_ID, 0L);

        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();

        sgFrameworkContextItem.setRequestParams(context.getParams());

        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setBiz(ScenarioConstant.BIZ_TYPE_SUPERMARKET);
        sceneInfo.setSubBiz(ScenarioConstant.LOC_TYPE_B2C);
        sceneInfo.setScene(ScenarioConstantApp.SCENARIO_YOU_BAO_ZANG);
        sgFrameworkContextItem.setSceneInfo(sceneInfo);

        UserDO userDO = new UserDO();
        userDO.setUserId(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getUserId).orElse(0L));
        userDO.setNick(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getNick).orElse(""));
        sgFrameworkContextItem.setUserDO(userDO);


        EntitySetParams entitySetParams = new EntitySetParams();
        entitySetParams.setItemSetIdList(Lists.newArrayList(itemSetId));

        sgFrameworkContextItem.setLocParams(CsaUtil.parseCsaObj(context.get(UserParamsKeyConstant.USER_PARAMS_KEY_CSA), smAreaId));
        sgFrameworkContextItem.setItemMetaInfo(this.getItemMetaInfo());


        PageInfoDO pageInfoDO = new PageInfoDO();
        pageInfoDO.setIndex(0);
        pageInfoDO.setPageSize(20);
        sgFrameworkContextItem.setUserPageInfo(pageInfoDO);

        return sgFrameworkServiceItem.recommend(sgFrameworkContextItem)
                .map(TacResult::newResult)
                .onErrorReturn(r -> TacResult.errorResult(""));
    }

    public static ItemMetaInfo getItemMetaInfo() {
        ItemMetaInfo itemMetaInfo = new ItemMetaInfo();
        List<ItemGroupMetaInfo> itemGroupMetaInfoList = Lists.newArrayList();
        List<ItemInfoSourceMetaInfo> itemInfoSourceMetaInfoList = Lists.newArrayList();
        ItemGroupMetaInfo itemGroupMetaInfo = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo);
        itemGroupMetaInfo.setGroupName("sm_B2C");
        itemGroupMetaInfo.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemGroupMetaInfo itemGroupMetaInfo1 = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo1);
        itemGroupMetaInfo1.setGroupName("sm_O2OOneHour");
        itemGroupMetaInfo1.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemGroupMetaInfo itemGroupMetaInfo2 = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo2);
        itemGroupMetaInfo2.setGroupName("sm_O2OHalfDay");
        itemGroupMetaInfo2.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemGroupMetaInfo itemGroupMetaInfo3 = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo3);
        itemGroupMetaInfo3.setGroupName("sm_O2ONextDay");
        itemGroupMetaInfo3.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoCaptain = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoCaptain.setSourceName("captain");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoCaptain);
        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoTpp = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoTpp.setSourceName("tpp");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoTpp);

        ItemRecommendMetaInfo itemRecommendMetaInfo = new ItemRecommendMetaInfo();
        itemRecommendMetaInfo.setAppId(18611L);


        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        return itemMetaInfo;
    }
}
