package com.tmall.wireless.tac.biz.processor.shangxinl.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.ItemEntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.model.meta.*;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceItem;
import com.tmall.txcs.gs.model.biz.context.EntitySetParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model.BannerItemDTO;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model.BannerItemVO;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model.BannerVO;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.uitl.BannerUtil;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 上新了超市商品推荐
 * @author haixiao.zhang
 * @date 2021/6/7
 */
@Component
public class SxlItemFeedsHandler extends RpmReactiveHandler<SgFrameworkResponse<EntityVO>> {

    Logger LOGGER = LoggerFactory.getLogger(SxlItemFeedsHandler.class);

    @Autowired
    SgFrameworkServiceItem sgFrameworkServiceItem;

    static List<Pair<String, String>> dataTubeKeyList = Lists.newArrayList(
        Pair.of("recommendWords","recommendWords"),
        Pair.of("videoUrl","videoUrl"),
        Pair.of("type","sxlType"),
        Pair.of("atmosphereImageUrl","atmosphereImageUrl"),
        Pair.of("sellingPointDesc","sellingPointDesc")
    );

    @Override
    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> executeFlowable(Context context) throws Exception {

        LOGGER.error("ITEM_REQUEST:{}", JSON.toJSONString(context));

        HadesLogUtil.debug("ITEM_REQUEST:{}"+JSON.toJSONString(context));

        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L);

        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();
        EntitySetParams entitySetParams = new EntitySetParams();
        entitySetParams.setItemSetSource("crm");
        Long itemSetId = (Long)context.getParams().get("itemSetId");
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

        sgFrameworkContextItem.setItemMetaInfo(getO2OItemMetaInfo());

        PageInfoDO pageInfoDO = new PageInfoDO();
        pageInfoDO.setIndex(0);
        pageInfoDO.setPageSize(20);
        sgFrameworkContextItem.setUserPageInfo(pageInfoDO);

        return sgFrameworkServiceItem.recommend(sgFrameworkContextItem)
            .map(TacResult::newResult)
            .onErrorReturn(r -> TacResult.errorResult(""));


    }

    private Map<String, BannerVO> convertResult(SgFrameworkResponse<EntityVO> response, String bannerInfo) {
        Map<String, BannerVO> result = Maps.newHashMap();

        Map<String, EntityVO> itemEntityVOMap = buildItemEntityVoMap(response);

        Map<String, List<BannerItemDTO>> bannerIndex2ItemList = BannerUtil.parseBannerItem(bannerInfo);

        bannerIndex2ItemList.keySet().forEach(key -> {
            BannerVO bannerVO = new BannerVO();
            List<BannerItemDTO> bannerItemDTOList = bannerIndex2ItemList.get(key);
            if (CollectionUtils.isEmpty(bannerItemDTOList)) {
                return;
            }

            List<Long> failItemList = Lists.newArrayList();
            List<Long> itemIdList = Lists.newArrayList();
            List<BannerItemVO> items = Lists.newArrayList();

            for (BannerItemDTO bannerItemDTO : bannerItemDTOList) {
                Long itemId = bannerItemDTO.getItemId();
                String locType = bannerItemDTO.getLocType();
                String s = locType + "_" + itemId;
                EntityVO entityVO = itemEntityVOMap.get(s);
                if (entityVO == null) {
                    failItemList.add(itemId);
                } else {
                    itemIdList.add(itemId);
                    BannerItemVO bannerItemVO = new BannerItemVO();
                    bannerItemVO.setItemId(itemId);
                    bannerItemVO.setLocType(locType);
                    bannerItemVO.setItemImg(entityVO.getString("itemImg"));
                    items.add(bannerItemVO);
                }
            }
            bannerVO.setFailItemList(failItemList);
            bannerVO.setItems(items);
            bannerVO.setEntryItems(Joiner.on(",").join(itemIdList));
            bannerVO.setItemIdList(itemIdList);
            result.put(key, bannerVO);
        });

        return result;
    }

    private Map<String, EntityVO> buildItemEntityVoMap(SgFrameworkResponse<EntityVO> response) {
        Map<String, EntityVO> itemEntityVOMap = Maps.newHashMap();

        if (response == null || CollectionUtils.isEmpty(response.getItemAndContentList())) {
            return itemEntityVOMap;
        }

        response.getItemAndContentList().stream().forEach(i -> {
            if (i instanceof ItemEntityVO) {
                Long itemId = i.getLong("itemId");
                String locType = i.getString("locType");
                String key = locType + "_" + itemId;
                itemEntityVOMap.put(key, i);
            }
        });
        return itemEntityVOMap;
    }

    public static ItemMetaInfo getO2OItemMetaInfo() {
        ItemMetaInfo itemMetaInfo = new ItemMetaInfo();
        List<ItemGroupMetaInfo> itemGroupMetaInfoList = Lists.newArrayList();
        List<ItemInfoSourceMetaInfo> itemInfoSourceMetaInfoList = Lists.newArrayList();
        ItemGroupMetaInfo itemGroupMetaInfo1 = new ItemGroupMetaInfo();
        itemGroupMetaInfoList.add(itemGroupMetaInfo1);
        itemGroupMetaInfo1.setGroupName("sm_B2C");
        itemGroupMetaInfo1.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoCaptain = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoCaptain.setSourceName("captain");
        itemInfoSourceMetaInfoCaptain.setSceneCode("shoppingguide.newLauch.common\n");
        itemInfoSourceMetaInfoCaptain.setDataTubeMateInfo(buildDataTubeMateInfo(322385L));

        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoCaptain);
        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoTpp = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoTpp.setSourceName("tpp");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoTpp);


        ItemRecommendMetaInfo itemRecommendMetaInfo = new ItemRecommendMetaInfo();
        itemRecommendMetaInfo.setAppId(24910L);
        itemMetaInfo.setItemRecommendMetaInfo(itemRecommendMetaInfo);
        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
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
