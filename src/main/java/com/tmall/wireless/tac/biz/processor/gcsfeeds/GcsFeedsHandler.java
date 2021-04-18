package com.tmall.wireless.tac.biz.processor.gcsfeeds;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MetaInfoUtil;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.model.meta.ContentMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemGroupMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemMetaInfo;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceContent;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by yangqing.byq on 2021/4/18.
 */
@Component
public class GcsFeedsHandler extends RpmReactiveHandler<SgFrameworkResponse<ContentVO>> {

    @Autowired
    SgFrameworkServiceContent sgFrameworkServiceContent;
    @Override
    public Flowable<TacResult<SgFrameworkResponse<ContentVO>>> executeFlowable(Context context) throws Exception {
        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L);

        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setBiz(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET);
        sceneInfo.setSubBiz(ScenarioConstantApp.LOC_TYPE_B2C);
        sceneInfo.setScene(ScenarioConstantApp.SCENARIO_GCS_FEEDS);

        SgFrameworkContextContent sgFrameworkContextContent = new SgFrameworkContextContent();
        UserDO userDO = new UserDO();
        userDO.setUserId((Long) Optional.of(context).map(Context::getUserInfo).map(UserInfo::getUserId).orElse(0L));
        userDO.setNick((String)Optional.of(context).map(Context::getUserInfo).map(UserInfo::getNick).orElse(""));
        sgFrameworkContextContent.setUserDO(userDO);
        sgFrameworkContextContent.setLocParams(CsaUtil.parseCsaObj(context.get("csa"), smAreaId));

        sgFrameworkContextContent.setContentMetaInfo(getContentMetaInfo());
        sgFrameworkContextContent.setSceneInfo(sceneInfo);
        return sgFrameworkServiceContent.recommend(sgFrameworkContextContent)
                .map(TacResult::newResult)
                .onErrorReturn(r -> TacResult.errorResult(""));
    }

    private ContentMetaInfo getContentMetaInfo() {

        ItemMetaInfo metaInfo = new ItemMetaInfo();
        List<ItemGroupMetaInfo> itemGroupMetaInfoList = Lists.newArrayList();
        metaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        ItemGroupMetaInfo itemGroupMetaInfo = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo);
        itemGroupMetaInfo.setGroupName("sm_B2C");
        List<ItemInfoSourceMetaInfo> itemInfoSourceMetaInfoList = Lists.newArrayList();
        itemGroupMetaInfo.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoSmartUi = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoSmartUi.setSourceName("smartui");
        itemInfoSourceMetaInfoSmartUi.setStrategyPackageId("508_8608");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoSmartUi);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoCaptain = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoCaptain.setSourceName("captain");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoCaptain);
        ContentMetaInfo contentMetaInfo = new ContentMetaInfo();
        contentMetaInfo.setNeedItem(true);
        contentMetaInfo.setItemMetaInfo(metaInfo);
        return contentMetaInfo;
    }


}
