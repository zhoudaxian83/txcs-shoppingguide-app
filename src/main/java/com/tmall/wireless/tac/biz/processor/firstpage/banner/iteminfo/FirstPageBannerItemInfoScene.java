package com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.ItemEntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.model.meta.ItemGroupMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemMetaInfo;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceItem;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.browsrec.BrowseRecommendScene;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model.BannerItemDTO;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model.BannerItemVO;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model.BannerVO;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.uitl.BannerUtil;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.config.ProcessTemplateSwitch;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by yangqing.byq on 2021/4/6.
 */
@Service
public class FirstPageBannerItemInfoScene {

    Logger LOGGER = LoggerFactory.getLogger(BrowseRecommendScene.class);


    @Autowired
    SgFrameworkServiceItem sgFrameworkServiceItem;

    public Flowable<TacResult<Map<String, BannerVO>>> recommend(Context context) {

        LOGGER.error("ITEM_REQUEST:{}", JSON.toJSONString(context));


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
        sgFrameworkContextItem.setItemMetaInfo(getBannerItemMetaInfo());


        PageInfoDO pageInfoDO = new PageInfoDO();
        pageInfoDO.setIndex(0);
        pageInfoDO.setPageSize(20);
        sgFrameworkContextItem.setUserPageInfo(pageInfoDO);

        return sgFrameworkServiceItem.recommend(sgFrameworkContextItem)
                .map(response -> convertResult(response, bannerInfo))
                .map(TacResult::newResult)
                .onErrorReturn(r -> TacResult.errorResult(""));

    }

    private Map<String, BannerVO> convertResult(SgFrameworkResponse<EntityVO> response, String bannerInfo) {
        Map<String, BannerVO> result = Maps.newHashMap();

        Map<String, EntityVO> itemEntityVOMap = buildItemEntityVoMap(response);

        Map<String, List<BannerItemDTO>> bannerIndex2ItemList = BannerUtil.parseBannerItem(bannerInfo);
        Set<Long> repeatItemIdSet = new HashSet<>();
        bannerIndex2ItemList.keySet().forEach(key -> {
            BannerVO bannerVO = new BannerVO();
            List<BannerItemDTO> bannerItemDTOList = bannerIndex2ItemList.get(key);
            if (CollectionUtils.isEmpty(bannerItemDTOList)) {
                return;
            }
            if (ProcessTemplateSwitch.openBubbleItemReduplicate) {
                if (CollectionUtils.isNotEmpty(bannerItemDTOList)) {
                    for (int i = 0; i < bannerItemDTOList.size(); i++) {
                        Long itemId = bannerItemDTOList.get(i).getItemId();
                        if (repeatItemIdSet.contains(itemId)) {//如果已经包含了，就继续下一个商品
                            continue;
                        } else {
                            //把当前不重复的商品换到第一个
                            Collections.swap(bannerItemDTOList, i, 0);
                            repeatItemIdSet.add(itemId);
                            break;
                        }
                    }
                }
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


    public static ItemMetaInfo getBannerItemMetaInfo() {
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

//        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoInv = new ItemInfoSourceMetaInfo();
//        itemInfoSourceMetaInfoInv.setSourceName("inventory");
//        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoInv);

        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);
        return itemMetaInfo;
    }
}
