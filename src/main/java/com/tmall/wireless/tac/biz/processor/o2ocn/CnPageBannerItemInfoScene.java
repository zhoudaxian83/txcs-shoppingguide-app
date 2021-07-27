package com.tmall.wireless.tac.biz.processor.o2ocn;

import java.util.List;
import java.util.Optional;

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
import com.tmall.txcs.gs.framework.model.meta.ItemRecommendMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.node.ItemInfoNode;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceItem;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by yangqing.byq on 2021/4/6.
 */
@Service
public class CnPageBannerItemInfoScene {

    @Autowired
    SgFrameworkServiceItem sgFrameworkServiceItem;

    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> recommend(Context context) {
        Long index = MapUtil.getLongWithDefault(context.getParams(), "index", 0L);
        Long pageSize = MapUtil.getLongWithDefault(context.getParams(), "pageSize", 20L);
        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L);


        String source = MapUtil.getStringWithDefault(context.getParams(), "source", "main");

        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();
        sgFrameworkContextItem.setRequestParams(context.getParams());
        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setBiz(ScenarioConstant.BIZ_TYPE_SUPERMARKET);
        sceneInfo.setSubBiz(ScenarioConstant.LOC_TYPE_O2O);
        sceneInfo.setScene(ScenarioConstantApp.O2O_CNXH);
        sgFrameworkContextItem.setSceneInfo(sceneInfo);

        UserDO userDO = new UserDO();
        userDO.setUserId(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getUserId).orElse(0L));
        userDO.setNick(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getNick).orElse(""));
        sgFrameworkContextItem.setUserDO(userDO);
        sgFrameworkContextItem.setLocParams(
            CsaUtil.parseCsaObj(context.get(UserParamsKeyConstant.USER_PARAMS_KEY_CSA), smAreaId));
        sgFrameworkContextItem.setItemMetaInfo(getItemMetaInfo());

        PageInfoDO pageInfoDO = new PageInfoDO();
        pageInfoDO.setIndex(index.intValue());
        pageInfoDO.setPageSize(pageSize.intValue());
        sgFrameworkContextItem.setUserPageInfo(pageInfoDO);
        sgFrameworkContextItem.setUserParams(context.getParams());
        String itemSetId = MapUtil.getStringWithDefault(context.getParams(), "itemSetId", "13545");
        sgFrameworkContextItem.getUserParams().put("itemSetId",itemSetId);
        sgFrameworkContextItem.getUserParams().put("source",source);


        if(StringUtils.isNotBlank(source)){
            return sgFrameworkServiceItem.recommend(sgFrameworkContextItem)
                .map(response->{
                    List<EntityVO> list = response.getItemAndContentList();
                    list.forEach(entityVO -> {
                        entityVO.put("itemUrl",entityVO.get("itemUrl")+"&sourceChannel="+source);
                    });
                    return response;
                })
                .map(TacResult::newResult)
                .onErrorReturn(r -> TacResult.errorResult(""));
        }else{
            return sgFrameworkServiceItem.recommend(sgFrameworkContextItem)
                .map(TacResult::newResult)
                .onErrorReturn(r -> TacResult.errorResult(""));
        }




    }

    public static ItemMetaInfo getItemMetaInfo() {
        ItemMetaInfo itemMetaInfo = new ItemMetaInfo();
        List<ItemGroupMetaInfo> itemGroupMetaInfoList = Lists.newArrayList();
        List<ItemInfoSourceMetaInfo> itemInfoSourceMetaInfoList = Lists.newArrayList();
        List<ItemInfoNode> itemInfoNodes = Lists.newArrayList();
        ItemInfoNode itemInfoNodeFirst = new ItemInfoNode();
        itemInfoNodes.add(itemInfoNodeFirst);
        itemInfoNodeFirst.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);

        ItemInfoNode itemInfoNodeSecond = new ItemInfoNode();
        itemInfoNodes.add(itemInfoNodeSecond);
        itemInfoNodeSecond.setItemInfoSourceMetaInfos(Lists.newArrayList(getItemInfoBySourceTimeLabel()));

        ItemGroupMetaInfo itemGroupMetaInfo = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo);
        itemGroupMetaInfo.setGroupName("sm_B2C");
        itemGroupMetaInfo.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemGroupMetaInfo itemGroupMetaInfo1 = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo1);
        itemGroupMetaInfo1.setGroupName("sm_O2OOneHour");
        itemGroupMetaInfo1.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        itemGroupMetaInfo1.setItemInfoNodes(itemInfoNodes);
        ItemGroupMetaInfo itemGroupMetaInfo2 = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo2);
        itemGroupMetaInfo2.setGroupName("sm_O2OHalfDay");
        itemGroupMetaInfo2.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        itemGroupMetaInfo2.setItemInfoNodes(itemInfoNodes);
        ItemGroupMetaInfo itemGroupMetaInfo3 = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo3);
        itemGroupMetaInfo3.setGroupName("sm_O2ONextDay");
        itemGroupMetaInfo3.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoCaptain = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoCaptain.setSourceName(ItemInfoSourceKey.CAPTAIN);
        itemInfoSourceMetaInfoCaptain.setSceneCode("index.guessULike");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoCaptain);
        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoTpp = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoTpp.setSourceName(ItemInfoSourceKey.TPP);
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoTpp);

        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);

        ItemRecommendMetaInfo itemRecommendMetaInfo = new ItemRecommendMetaInfo();
        itemRecommendMetaInfo.setAppId(21895L);
        itemMetaInfo.setItemRecommendMetaInfo(itemRecommendMetaInfo);

        return itemMetaInfo;
    }


     private static ItemInfoSourceMetaInfo getItemInfoBySourceTimeLabel() {

        ItemInfoSourceMetaInfo itemInfoSourceMetaInfo = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfo.setSourceName("timeLabel");
        return itemInfoSourceMetaInfo;
    }
}
