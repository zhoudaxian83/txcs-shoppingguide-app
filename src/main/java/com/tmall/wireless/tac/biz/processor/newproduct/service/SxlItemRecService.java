package com.tmall.wireless.tac.biz.processor.newproduct.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.*;
import com.tmall.txcs.gs.framework.model.meta.*;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceItem;
import com.tmall.txcs.gs.model.biz.context.EntitySetParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.config.SxlSwitch;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author haixiao.zhang
 * @date 2021/6/9
 */
@Service
public class SxlItemRecService {

    Logger LOGGER = LoggerFactory.getLogger(SxlItemRecService.class);

    @Autowired
    TacLogger tacLogger;

    @Autowired
    SgFrameworkServiceItem sgFrameworkServiceItem;

    static List<Pair<String, String>> dataTubeKeyList = Lists.newArrayList(
        Pair.of("recommendWords","recommendWords"),
        Pair.of("videoUrl","videoUrl"),
        Pair.of("type","type"),
        Pair.of("atmosphereImageUrl","atmosphereImageUrl"),
        Pair.of("sellingPointDesc","sellingPointDesc")
    );

    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> recommend(Context context) {
        LOGGER.error("ITEM_REQUEST:{}", JSON.toJSONString(context));

        HadesLogUtil.debug("ITEM_REQUEST:{}"+JSON.toJSONString(context));

        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L);
        Long itemSetId = MapUtil.getLongWithDefault(context.getParams(), RequestKeyConstantApp.ITEMSET_ID, 322385L);

        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();
        EntitySetParams entitySetParams = new EntitySetParams();
        entitySetParams.setItemSetSource("crm");
        entitySetParams.setItemSetIdList(Lists.newArrayList(itemSetId));
        sgFrameworkContextItem.setRequestParams(context.getParams());
        sgFrameworkContextItem.setEntitySetParams(entitySetParams);
        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setBiz(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET);
        sceneInfo.setSubBiz(ScenarioConstantApp.LOC_TYPE_B2C);
        sceneInfo.setScene(ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM);
        sgFrameworkContextItem.setSceneInfo(sceneInfo);
        UserDO userDO = new UserDO();
        userDO.setUserId(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getUserId).orElse(0L));
        userDO.setNick(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getNick).orElse(""));
        sgFrameworkContextItem.setUserDO(userDO);

        sgFrameworkContextItem.setLocParams(CsaUtil
            .parseCsaObj(context.get(UserParamsKeyConstant.USER_PARAMS_KEY_CSA), smAreaId));

        sgFrameworkContextItem.setItemMetaInfo(getItemMetaInfo());

        PageInfoDO pageInfoDO = new PageInfoDO();
        String index = MapUtil.getStringWithDefault(context.getParams(), RequestKeyConstantApp.INDEX, "0");
        String pageSize = MapUtil.getStringWithDefault(context.getParams(), RequestKeyConstantApp.PAGE_SIZE, "10");
        pageInfoDO.setIndex(Integer.valueOf(index));
        pageInfoDO.setPageSize(Integer.valueOf(pageSize));
        sgFrameworkContextItem.setUserPageInfo(pageInfoDO);


        tacLogger.info("switch1:"+SxlSwitch.ITEM_PAGE_SIZE);

        return sgFrameworkServiceItem.recommend(sgFrameworkContextItem)
            .map(TacResult::newResult)
            .onErrorReturn(r -> TacResult.errorResult(""));

    }

    private static ItemMetaInfo getItemMetaInfo() {
        ItemMetaInfo itemMetaInfo = new ItemMetaInfo();
        List<ItemGroupMetaInfo> itemGroupMetaInfoList = Lists.newArrayList();
        List<ItemInfoSourceMetaInfo> itemInfoSourceMetaInfoList = Lists.newArrayList();
        ItemGroupMetaInfo itemGroupMetaInfo1 = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo1);
        itemGroupMetaInfo1.setGroupName("sm_B2C");
        itemGroupMetaInfo1.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoCaptain = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoCaptain.setSourceName("captain");
        itemInfoSourceMetaInfoCaptain.setSceneCode("shoppingguide.newLauch.common");
        itemInfoSourceMetaInfoCaptain.setDataTubeMateInfo(buildDataTubeMateInfo(322385L));

        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoCaptain);
        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoTpp = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoTpp.setSourceName("tpp");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoTpp);


        ItemRecommendMetaInfo itemRecommendMetaInfo = new ItemRecommendMetaInfo();
        itemRecommendMetaInfo.setAppId(25385L);
        itemMetaInfo.setItemRecommendMetaInfo(itemRecommendMetaInfo);
        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        itemRecommendMetaInfo.setUseRecommendSpiV2(true);
        return itemMetaInfo;
    }


    private static DataTubeMateInfo buildDataTubeMateInfo(Long itemSetId) {


        DataTubeMateInfo dataTubeMateInfo = new DataTubeMateInfo();
        dataTubeMateInfo.setActivityId(String.valueOf(itemSetId));
        dataTubeMateInfo.setChannelName("itemExtLdb");
        dataTubeMateInfo.setDataKeyList(dataTubeKeyList.stream().map(k -> {
            DataTubeKey dataTubeKey = new DataTubeKey();
            dataTubeKey.setDataKey(k.getRight());
            dataTubeKey.setVoKey(k.getLeft());
            return dataTubeKey;
        }).collect(Collectors.toList()));
        return dataTubeMateInfo;
    }

}
