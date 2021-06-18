package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.List;
import java.util.Optional;

import com.ali.unit.rule.util.lang.CollectionUtils;
import com.google.common.collect.Lists;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.model.constant.ItemInfoSourceKey;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.framework.model.meta.ItemGroupMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemMetaInfo;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceItem;
import com.tmall.txcs.gs.model.biz.context.EntitySetParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.PmtParams;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.wzt.utils.LimitItemUtil;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by yangqing.byq on 2021/4/6.
 */
@Service
public class WuZheTianPageBannerItemInfoScene {
    @Autowired
    TacLogger tacLogger;

    @Autowired
    SgFrameworkServiceItem sgFrameworkServiceItem;

    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> recommend(Context context) {
        Long level1Id = MapUtil.getLongWithDefault(context.getParams(), "level1Id", 0L);
        Long index = MapUtil.getLongWithDefault(context.getParams(), "index", 0L);
        Long pageSize = MapUtil.getLongWithDefault(context.getParams(), "pageSize", 20L);
        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L);

        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();
        sgFrameworkContextItem.setRequestParams(context.getParams());
        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setBiz(ScenarioConstant.BIZ_TYPE_SUPERMARKET);
        sceneInfo.setSubBiz(ScenarioConstant.LOC_TYPE_B2C);
        sceneInfo.setScene(ScenarioConstantApp.WU_ZHE_TIAN);
        sgFrameworkContextItem.setSceneInfo(sceneInfo);

        UserDO userDO = new UserDO();
        userDO.setUserId(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getUserId).orElse(0L));
        userDO.setNick(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getNick).orElse(""));
        sgFrameworkContextItem.setUserDO(userDO);
        sgFrameworkContextItem.setLocParams(
                CsaUtil.parseCsaObj(context.get(UserParamsKeyConstant.USER_PARAMS_KEY_CSA), smAreaId));
        sgFrameworkContextItem.setItemMetaInfo(getItemMetaInfo());

        EntitySetParams entitySetParams = new EntitySetParams();
        entitySetParams.setItemSetSource("crm");
        entitySetParams.setItemSetIdList(Lists.newArrayList(5233L));
        sgFrameworkContextItem.setEntitySetParams(entitySetParams);

        PmtParams pmtParams = new PmtParams();
        pmtParams.setPmtSource("sm_manager");
        pmtParams.setPmtName("wuZheTian");
        pmtParams.setPageId("wuZheTian");
        pmtParams.setModuleId(level1Id.toString());
        sgFrameworkContextItem.setPmtParams(pmtParams);

        PageInfoDO pageInfoDO = new PageInfoDO();
        pageInfoDO.setIndex(index.intValue());
        pageInfoDO.setPageSize(pageSize.intValue());
        sgFrameworkContextItem.setUserPageInfo(pageInfoDO);
        sgFrameworkContextItem.setUserParams(context.getParams());
        return sgFrameworkServiceItem.recommend(sgFrameworkContextItem)
                .map(TacResult::newResult).map(tacResult -> {
                    List<EntityVO> originalEntityVOList = tacResult.getData().getItemAndContentList();
                    if (!CollectionUtils.isEmpty(originalEntityVOList)) {
                        List<EntityVO> noLimitEntityVOList = LimitItemUtil.doLimitItems(originalEntityVOList);
                        if (noLimitEntityVOList.size() != originalEntityVOList.size()) {
                            tacResult.getData().setItemAndContentList(noLimitEntityVOList);
                        }
                    }
                    return tacResult;
                })
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
        itemInfoSourceMetaInfoCaptain.setSourceName(ItemInfoSourceKey.CAPTAIN);
        itemInfoSourceMetaInfoCaptain.setSceneCode("superMarket_todayCrazy");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoCaptain);
        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoTpp = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoTpp.setSourceName(ItemInfoSourceKey.TPP);
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoTpp);


        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoTest = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoTest.setSourceName("test");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoTest);

        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        return itemMetaInfo;
    }
}
