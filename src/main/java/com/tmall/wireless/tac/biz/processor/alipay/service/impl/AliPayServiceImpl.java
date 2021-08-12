package com.tmall.wireless.tac.biz.processor.alipay.service.impl;

import com.alibaba.aladdin.lamp.domain.request.Request;
import com.alibaba.aladdin.lamp.domain.request.RequestItem;
import com.alibaba.aladdin.lamp.domain.request.modules.LocationInfo;
import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.aladdin.lamp.domain.response.ResResponse;
import com.alibaba.aladdin.lamp.domain.user.UserProfile;
import com.alibaba.fastjson.JSONObject;
import com.alipay.recmixer.common.service.facade.model.CategoryContentRet;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecRequest;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecResult;
import com.alipay.recmixer.common.service.facade.model.ServiceContentRec;
import com.alipay.tradecsa.common.service.spi.request.MiddlePageSPIRequest;
import com.alipay.tradecsa.common.service.spi.response.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.txcs.gs.spi.recommend.AldSpi;
import com.tmall.wireless.store.spi.user.UserProvider;
import com.tmall.wireless.store.spi.user.base.UicDeliverAddressBO;
import com.tmall.wireless.tac.biz.processor.alipay.constant.AliPayConstant;
import com.tmall.wireless.tac.biz.processor.alipay.service.IAliPayService;
import com.tmall.wireless.tac.biz.processor.alipay.service.ext.AliPayFirstPageBuildItemVoSdkExtPt;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;

@Service("aliPayServiceImpl")
public class AliPayServiceImpl implements IAliPayService {


    public static final String itemSetAldKey = "itemSet";
    public static final String hookItemSetAldKey = "hookItemSet";
    public static final String itemLabelAldKey = "itemLabel";
    public static final String fpTitleAldKey = "fpTitle";
    public static final String fpServiceTextAldKey = "fpService";
    public static final String fpIconPicAldKey = "fpIconPic";

    public static final String headColorAldKey = "headColor";
    public static final String headSubTitleAldKey = "headSubTitle";
    public static final String headBgPicAldKey = "headBgPic";
    public static final String navigationTitleAldKey = "navigationTitle";
    public static final String navigationIconPicAldKey = "navigationIconPic";
    public static final String navigationSearchUrlAldKey = "navigationSearchUrl";

    public static final String cardTitleAldKey = "cardTitle";
    public static final String cardSubTitleAldKey = "cardSubTitle";
    public static final String cardBgPicAldKey = "cardBgPic";


    public static final String ALD_RES_ID = "18639997";

    @Autowired
    UserProvider userProvider;

    @Autowired
    private AldSpi aldSpi;


    @Autowired
    ShoppingguideSdkItemService shoppingguideSdkItemService;

    @Override
    public Flowable<MixerCollectRecResult> processFirstPage(Context context, MixerCollectRecRequest mixerCollectRecRequest) {
        SPIResult<Map<String, Long>> uicIdFromAlipayUid = userProvider.getUicIdFromAlipayUid(Lists.newArrayList("2088602128328730"));

        Long taobaoUserId = Optional.of(uicIdFromAlipayUid).map(SPIResult::getData).map(map -> map.get("2088602128328730")).orElse(0L);

        SPIResult<UicDeliverAddressBO> userDefaultAddressSyn = userProvider.getUserDefaultAddressSyn(357133924L);

        String devisionCode = Optional.of(userDefaultAddressSyn).map(SPIResult::getData).map(UicDeliverAddressBO::getDevisionCode).orElse("");

        GeneralItem aldData = getAldData(taobaoUserId, devisionCode);

        BizScenario bizScenario = BizScenario.valueOf(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                ScenarioConstantApp.SCENARIO_ALI_PAY_FIRST_PAGE);


        return shoppingguideSdkItemService.recommend(context, bizScenario)
                .map(re -> {
                    MixerCollectRecResult mixerCollectRecResult = new MixerCollectRecResult();
                    mixerCollectRecResult.setSuccess(true);
                    CategoryContentRet categoryContentRet = new CategoryContentRet();
                    Map<String, CategoryContentRet> categoryContentRetMap = Maps.newHashMap();
                    categoryContentRetMap.put(AliPayConstant.CATEGORY_CODE, categoryContentRet);
                    mixerCollectRecResult.setCategoryContentMap(categoryContentRetMap);
                    List<ServiceContentRec>	serviceContentRecList = Lists.newArrayList();
                    categoryContentRet.setTitle(aldData.getString(fpTitleAldKey));
                    categoryContentRet.setSubTitle(aldData.getString(fpServiceTextAldKey));
                    categoryContentRet.setActionImgUrl(aldData.getString(fpServiceTextAldKey));
                    categoryContentRet.setServiceList(serviceContentRecList);
                    List<ServiceContentRec> collect = re.getItemAndContentList().stream().map(this::convert).collect(Collectors.toList());
                    categoryContentRet.setServiceList(collect);
                    categoryContentRet.setSuccess(true);
                    return mixerCollectRecResult;
                });

    }

    @Override
    public Flowable<MiddlePageSPIResponse> processMiddlePage(Context context, MiddlePageSPIRequest middlePageSPIRequest) {

        GeneralItem aldData = getAldData(357133924L, "330100");

        MiddlePageSPIResponse middlePageSPIResponse1 = new MiddlePageSPIResponse();

        // 头部区
        PageFloorHeaderDTO pageFloorHeaderDTO = new PageFloorHeaderDTO();
        pageFloorHeaderDTO.setType("image");
        pageFloorHeaderDTO.setTitle(aldData.getString(headSubTitleAldKey));
        pageFloorHeaderDTO.setSubtitle(aldData.getString(headSubTitleAldKey));
        pageFloorHeaderDTO.setBgColor(aldData.getString(headColorAldKey));
        pageFloorHeaderDTO.setSubTitleImgUrl(aldData.getString(headBgPicAldKey));

        // 导航栏
        PageFloorNavigationDTO pageFloorNavigationDTO = new PageFloorNavigationDTO();
        pageFloorNavigationDTO.setTitle(aldData.getString(navigationTitleAldKey));
        pageFloorNavigationDTO.setTitleImage(aldData.getString(navigationIconPicAldKey));
        pageFloorNavigationDTO.setStyle("light");
        pageFloorNavigationDTO.setTitleLightImageUrl(aldData.getString(navigationIconPicAldKey));
        middlePageSPIResponse1.setPageFloorNavigationDTO(pageFloorNavigationDTO);

        BizScenario bizScenario = BizScenario.valueOf(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                ScenarioConstantApp.SCENARIO_ALI_PAY_FIRST_PAGE);


         return shoppingguideSdkItemService.recommend(context, bizScenario).map(re -> {

            List<ItemEntityVO> itemAndContentList = re.getItemAndContentList();

            PageFloorResultDTO pageFloorResultDTO = new PageFloorResultDTO();
            pageFloorResultDTO.setPageFloorId("111");
            PageFloorResultDetailDTO pageFloorResultDetailDTO = new PageFloorResultDetailDTO();
            pageFloorResultDTO.setPageFloorResultDetailDTO(pageFloorResultDetailDTO);
            PageFloorAtomicResultDTO pageFloorAtomicResultDTO1 = new PageFloorAtomicResultDTO();
            pageFloorResultDetailDTO.setPageFloorAtomicResultDTOList(Lists.newArrayList(pageFloorAtomicResultDTO1));
            List<JSONObject> cardData1 = Lists.newArrayList();
            pageFloorAtomicResultDTO1.setCardData(cardData1);
            cardData1.add(itemAndContentList.get(0));



            PageFloorResultDTO pageFloorResultDTO2 = new PageFloorResultDTO();
            pageFloorResultDTO2.setPageFloorId("222");
            PageFloorResultDetailDTO pageFloorResultDetailDTO2 = new PageFloorResultDetailDTO();
            pageFloorResultDTO2.setPageFloorResultDetailDTO(pageFloorResultDetailDTO2);
            PageFloorAtomicResultDTO pageFloorAtomicResultDTO2 = new PageFloorAtomicResultDTO();
            pageFloorResultDetailDTO2.setPageFloorAtomicResultDTOList(Lists.newArrayList(pageFloorAtomicResultDTO2));
            List<JSONObject> cardData2 = Lists.newArrayList();
            cardData2.addAll(itemAndContentList);
            pageFloorAtomicResultDTO2.setCardData(cardData2);

            middlePageSPIResponse1.setPageFloorResultDTOList(Lists.newArrayList(pageFloorResultDTO2, pageFloorResultDTO));


            return middlePageSPIResponse1;
        });

    }


    private ServiceContentRec convert(ItemEntityVO item) {
        ServiceContentRec serviceContentRec = new ServiceContentRec();
        serviceContentRec.setItemId(String.valueOf(item.getItemId()));
        serviceContentRec.setImgUrl(item.getString("itemImg"));

        serviceContentRec.setTitle(item.getString("title"));
        serviceContentRec.setActionLink(item.getString("itemUrl"));
        serviceContentRec.setBizCode(AliPayConstant.BIZ_CODE);
        serviceContentRec.setSource(AliPayConstant.SOURCE);

        Map<String, String> ext = Maps.newHashMap();
        ext.put("subScript", item.getString(AliPayFirstPageBuildItemVoSdkExtPt.PROMOTION_POINT));
        ext.put("subTitle", item.getString(AliPayFirstPageBuildItemVoSdkExtPt.PROMOTION_POINT));
        ext.put("sellingPrice", item.getString(AliPayFirstPageBuildItemVoSdkExtPt.SELLING_PRICE));
        ext.put("originPrice", item.getString(AliPayFirstPageBuildItemVoSdkExtPt.ORIGIN_PRICE));

        serviceContentRec.setExtMap(ext);

        return serviceContentRec;
    }

    GeneralItem getAldData(Long userId, String smAreaId) {
        Request request = buildAldRequest(userId, smAreaId);
        Map<String, ResResponse> stringResResponseMap =
                aldSpi.queryAldInfoSync(request);
        List<GeneralItem> generalItemList = (List<GeneralItem>) Optional.of(stringResResponseMap).map(m -> m.get(ALD_RES_ID)).map(ResResponse::getData).orElse(null);
        if (CollectionUtils.isNotEmpty(generalItemList)) {
            return generalItemList.get(0);
        } else {
            return null;
        }
    }

    private Request buildAldRequest(Long userId, String smAreaId) {

        Request re = new Request();
        re.setResIds(ALD_RES_ID);

        LocationInfo locationInfo = new LocationInfo();
        //四级地址
        locationInfo.setCityLevel4(smAreaId);

        re.setLocationInfo(locationInfo);
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(userId);
        re.setUserProfile(userProfile);
        RequestItem requestItem = new RequestItem();
        requestItem.setResId(ALD_RES_ID);
        re.setRequestItems(Lists.newArrayList(requestItem));
        return re;
    }
}
