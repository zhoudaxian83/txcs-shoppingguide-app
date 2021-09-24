package com.tmall.wireless.tac.biz.processor.gul.promotion;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.google.common.collect.Lists;
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
import com.tmall.txcs.gs.model.biz.context.EntitySetParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.PmtParams;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author guijian
 */
@Service
public class GulPromotionScene {

    @Autowired
    SgFrameworkServiceItem sgFrameworkServiceItem;

    private static final Long appId = 26777L;

    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> recommend(Context context) {
        Long level1Id = MapUtil.getLongWithDefault(context.getParams(), "level1Id", 1942L);
        /*Long level1Id = MapUtil.getLongWithDefault(context.getParams(), "level1Id", 1217L);*/
        /*Long level1Id = MapUtil.getLongWithDefault(context.getParams(), "level1Id", 22705L);*/
        Long level2Id = MapUtil.getLongWithDefault(context.getParams(), "level2Id", 0L);
        int index = Integer.parseInt(MapUtil.getStringWithDefault(context.getParams(), "index", "0"));
        int pageSize = Integer.valueOf(MapUtil.getStringWithDefault(context.getParams(), "pageSize", "20"));
        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L);

        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();
        sgFrameworkContextItem.setRequestParams(context.getParams());
        sgFrameworkContextItem.setSceneInfo(getSceneInfo());
        sgFrameworkContextItem.setUserDO(getUserDO(context));

        PageInfoDO pageInfoDO = new PageInfoDO();
        pageInfoDO.setIndex(index);
        pageInfoDO.setPageSize(pageSize);
        sgFrameworkContextItem.setUserPageInfo(pageInfoDO);
        sgFrameworkContextItem.setLocParams(CsaUtil.parseCsaObj(context.get(UserParamsKeyConstant.USER_PARAMS_KEY_CSA), smAreaId));

        EntitySetParams entitySetParams = new EntitySetParams();
        entitySetParams.setItemSetSource("crm");
        entitySetParams.setItemSetIdList(Lists.newArrayList(new Long[]{5233L}));
        sgFrameworkContextItem.setEntitySetParams(entitySetParams);
        PmtParams pmtParams = new PmtParams();
        pmtParams.setPmtSource("sm_manager");
        pmtParams.setPmtName("guessULike");
        //pmtParams.setPageId("cainixihuan1");
        pmtParams.setPageId("promotionCainixihuan");
        pmtParams.setModuleId(level1Id.toString());
        pmtParams.setTagId(level2Id.toString());
        sgFrameworkContextItem.setPmtParams(pmtParams);

        sgFrameworkContextItem.setItemMetaInfo(getGulSubTabItemMetaInfo());


        return this.sgFrameworkServiceItem.recommend(sgFrameworkContextItem).map(TacResult::newResult).onErrorReturn((r) -> {
            return TacResult.errorResult("");
        });

    }

    public SceneInfo getSceneInfo(){
        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setBiz(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET);
        sceneInfo.setSubBiz(ScenarioConstantApp.LOC_TYPE_B2C);
        sceneInfo.setScene(ScenarioConstantApp.GUL_PROMOTION);
        return sceneInfo;
    }
    public UserDO getUserDO(Context context){
        UserDO userDO = new UserDO();
        userDO.setUserId(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getUserId).orElse(0L));
        userDO.setNick(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getNick).orElse(""));
        if (MapUtils.isNotEmpty(context.getParams())) {
            Object cookies = context.getParams().get("cookies");
            if (cookies != null && cookies instanceof Map) {
                String cna = (String)((Map)cookies).get("cna");
                userDO.setCna(cna);
            }
        }
        return userDO;
    }

    public static ItemMetaInfo getGulSubTabItemMetaInfo() {
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
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoSmartUi = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoSmartUi.setSourceName("smartui");
        itemInfoSourceMetaInfoSmartUi.setStrategyPackageId("637_10576");
        itemInfoSourceMetaInfoSmartUi.setAppId(appId);
        /*itemInfoSourceMetaInfoSmartUi.setStrategyPackageId("508_8608");*/
       /* List<String> e1 = Lists.newArrayList(new String[]{"supermarketPrice", "timesBot", "salesLast30d"});
        List<String> e2 = Lists.newArrayList(new String[]{"priceLabel", "timesBot", "salesLast30d"});
        List<List<String>> exclusiveMaterials = Lists.newArrayList();
        exclusiveMaterials.add(e1);
        exclusiveMaterials.add(e2);
        itemInfoSourceMetaInfoSmartUi.setExclusiveMaterials(exclusiveMaterials);*/
        List<String> requireList = Lists.newArrayList(new String[]{"extVideo5S", "richPict", "whitePict"});
        List<String> requireListPrice = Lists.newArrayList(new String[]{"pagePrice"});
        List<List<String>> requireListList = Lists.newArrayList();
        requireListList.add(requireList);
        requireListList.add(requireListPrice);
        itemInfoSourceMetaInfoSmartUi.setRequiredMaterials(requireListList);
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoSmartUi);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoCaptain = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoCaptain.setSourceName("captain");
        /*itemInfoSourceMetaInfoCaptain.setSceneCode("index.guessULike");*/
        itemInfoSourceMetaInfoCaptain.setSceneCode("conference.promotion");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoCaptain);
        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoTpp = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoTpp.setSourceName("tpp");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoTpp);
        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        return itemMetaInfo;
    }

}
