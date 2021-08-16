package com.tmall.wireless.tac.biz.processor.alipay.service.impl;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSONObject;
import com.alipay.recmixer.common.service.facade.model.CategoryContentRet;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecRequest;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecResult;
import com.alipay.recmixer.common.service.facade.model.ServiceContentRec;
import com.alipay.tradecsa.common.service.spi.request.MiddlePageFloorDTO;
import com.alipay.tradecsa.common.service.spi.request.MiddlePageSPIRequest;
import com.alipay.tradecsa.common.service.spi.request.PageFloorAtomicDTO;
import com.alipay.tradecsa.common.service.spi.request.PageFloorDetailDTO;
import com.alipay.tradecsa.common.service.spi.response.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.wireless.tac.biz.processor.alipay.constant.AliPayConstant;
import com.tmall.wireless.tac.biz.processor.alipay.service.IAliPayService;
import com.tmall.wireless.tac.biz.processor.alipay.service.ext.AliPayFirstPageBuildItemVoSdkExtPt;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.AtomicCardProcessRequest;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.AtomicCardProcessorFactory;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.IAtomicCardProcessor;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tmall.wireless.tac.biz.processor.alipay.service.ext.AliPayItemUserCommonParamsBuildSdkExtPt.CONTEXT_KEY;

@Service("aliPayServiceImpl")
public class AliPayServiceImpl implements IAliPayService {

    @Autowired
    AtomicCardProcessorFactory atomicCardProcessorFactory;

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


    @Autowired
    AldService aldService;





    @Autowired
    ShoppingguideSdkItemService shoppingguideSdkItemService;

    @Override
    public Flowable<MixerCollectRecResult> processFirstPage(Context context) {

        BizScenario bizScenario = BizScenario.valueOf(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                ScenarioConstantApp.SCENARIO_ALI_PAY_FIRST_PAGE);

        return shoppingguideSdkItemService.recommend(context, bizScenario)
                .map(re -> convertMixerCollectRecResult(re, (GeneralItem) context.get(CONTEXT_KEY)));

    }

    private MixerCollectRecResult convertMixerCollectRecResult(SgFrameworkResponse<ItemEntityVO> re, GeneralItem aldData) {
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
        List<ServiceContentRec> collect = re.getItemAndContentList().stream().map(e -> convert(e, aldData)).collect(Collectors.toList());
        categoryContentRet.setServiceList(collect);
        categoryContentRet.setSuccess(true);
        return mixerCollectRecResult;
    }

    @Override
    public Flowable<MiddlePageSPIResponse> processMiddlePage(Context context, MiddlePageSPIRequest middlePageSPIRequest) {

        return null;
//        GeneralItem aldData = getAldData(357133924L, "330100");
//
//        MiddlePageSPIResponse middlePageSPIResponse = new MiddlePageSPIResponse();
//
//        // 头部区
//        PageFloorHeaderDTO pageFloorHeaderDTO = new PageFloorHeaderDTO();
//        pageFloorHeaderDTO.setType("image");
//        pageFloorHeaderDTO.setTitle(aldData.getString(headSubTitleAldKey));
//        pageFloorHeaderDTO.setSubtitle(aldData.getString(headSubTitleAldKey));
//        pageFloorHeaderDTO.setBgColor(aldData.getString(headColorAldKey));
//        pageFloorHeaderDTO.setSubTitleImgUrl(aldData.getString(headBgPicAldKey));
//
//        // 导航栏
//        PageFloorNavigationDTO pageFloorNavigationDTO = new PageFloorNavigationDTO();
//        pageFloorNavigationDTO.setTitle(aldData.getString(navigationTitleAldKey));
//        pageFloorNavigationDTO.setTitleImage(aldData.getString(navigationIconPicAldKey));
//        pageFloorNavigationDTO.setStyle("light");
//        pageFloorNavigationDTO.setTitleLightImageUrl(aldData.getString(navigationIconPicAldKey));
//        middlePageSPIResponse.setPageFloorNavigationDTO(pageFloorNavigationDTO);
//
//        BizScenario bizScenario = BizScenario.valueOf(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
//                ScenarioConstantApp.LOC_TYPE_B2C,
//                ScenarioConstantApp.SCENARIO_ALI_PAY_FIRST_PAGE);
//
//
//        return shoppingguideSdkItemService.recommend(context, bizScenario).map(re -> {
//
//            List<ItemEntityVO> itemAndContentList = re.getItemAndContentList();
//            List<PageFloorResultDTO> floorResultDTOS = Lists.newArrayList();
//            middlePageSPIRequest.getMiddlePageFloorDTOList().forEach(middlePageFloorDTO -> {
//
//
//                PageFloorResultDTO pageFloorResultDTO = new PageFloorResultDTO();
//                pageFloorResultDTO.setPageFloorId(middlePageFloorDTO.getPageFloorId());
//
//                List<PageFloorAtomicDTO> pageFloorAtomicDTOS = Optional.of(middlePageFloorDTO).map(MiddlePageFloorDTO::getPageFloorDetailDTO).map(PageFloorDetailDTO::getPageFloorAtomicDTOList).orElse(Lists.newArrayList());
//                List<PageFloorAtomicResultDTO> pageFloorAtomicResultDTOList = pageFloorAtomicDTOS.stream()
//                        .map(this::processAtomic)
//                        .filter(Objects::nonNull)
//                        .collect(Collectors.toList());
//                PageFloorResultDetailDTO pageFloorResultDetailDTO = new PageFloorResultDetailDTO();
//                pageFloorResultDetailDTO.setPageFloorAtomicResultDTOList(pageFloorAtomicResultDTOList);
//                pageFloorResultDTO.setPageFloorResultDetailDTO(pageFloorResultDetailDTO);
//
//                floorResultDTOS.add(pageFloorResultDTO);
//
//            });
//
//            middlePageSPIResponse.setPageFloorResultDTOList(floorResultDTOS);
//
//            PageFloorResultDTO pageFloorResultDTO = new PageFloorResultDTO();
//            pageFloorResultDTO.setPageFloorId("111");
//            PageFloorResultDetailDTO pageFloorResultDetailDTO = new PageFloorResultDetailDTO();
//            pageFloorResultDTO.setPageFloorResultDetailDTO(pageFloorResultDetailDTO);
//            PageFloorAtomicResultDTO pageFloorAtomicResultDTO1 = new PageFloorAtomicResultDTO();
//            pageFloorAtomicResultDTO1.setAtomCardTemplateId("原子模版ID");
//            pageFloorAtomicResultDTO1.setDataId("???");
//            pageFloorResultDetailDTO.setPageFloorAtomicResultDTOList(Lists.newArrayList(pageFloorAtomicResultDTO1));
//            List<JSONObject> cardData1 = Lists.newArrayList();
//            pageFloorAtomicResultDTO1.setCardData(cardData1);
//            cardData1.add(itemAndContentList.get(0));
//            cardData1.add(itemAndContentList.get(0));
//
//
//
//            PageFloorResultDTO pageFloorResultDTO2 = new PageFloorResultDTO();
//            pageFloorResultDTO2.setPageFloorId("222");
//            PageFloorResultDetailDTO pageFloorResultDetailDTO2 = new PageFloorResultDetailDTO();
//            pageFloorResultDTO2.setPageFloorResultDetailDTO(pageFloorResultDetailDTO2);
//            PageFloorAtomicResultDTO pageFloorAtomicResultDTO2 = new PageFloorAtomicResultDTO();
//            pageFloorResultDetailDTO2.setPageFloorAtomicResultDTOList(Lists.newArrayList(pageFloorAtomicResultDTO2));
//            List<JSONObject> cardData2 = Lists.newArrayList();
//            cardData2.addAll(itemAndContentList);
//            pageFloorAtomicResultDTO2.setCardData(cardData2);
//
//            middlePageSPIResponse.setPageFloorResultDTOList(Lists.newArrayList(pageFloorResultDTO2, pageFloorResultDTO));
//
//
//            return middlePageSPIResponse;
//        });

    }

    private PageFloorAtomicResultDTO processAtomic(PageFloorAtomicDTO pageFloorAtomicDTO) {

        IAtomicCardProcessor processorByFloorId = atomicCardProcessorFactory.getProcessorByFloorId(pageFloorAtomicDTO.getAtomCardTemplateId());
        if (processorByFloorId == null) {
            return null;
        }

        AtomicCardProcessRequest atomicCardProcessRequest = new AtomicCardProcessRequest();
        atomicCardProcessRequest.setPageFloorAtomicDTO(pageFloorAtomicDTO);

        return processorByFloorId.process(atomicCardProcessRequest);
    }


    private ServiceContentRec convert(ItemEntityVO item, GeneralItem aldData) {
        ServiceContentRec serviceContentRec = new ServiceContentRec();
        serviceContentRec.setItemId(String.valueOf(item.getItemId()));
        serviceContentRec.setImgUrl(item.getString("itemImg"));

        serviceContentRec.setTitle(item.getString("shortTitle"));
        serviceContentRec.setActionLink(item.getString("itemUrl"));
        serviceContentRec.setBizCode(AliPayConstant.BIZ_CODE);
        serviceContentRec.setSource(AliPayConstant.SOURCE);

        String subTitle = item.getString(AliPayFirstPageBuildItemVoSdkExtPt.PROMOTION_POINT);
        subTitle = StringUtils.isEmpty(subTitle) ? aldData.getString(itemLabelAldKey) : subTitle;
        Map<String, String> ext = Maps.newHashMap();
        ext.put("subScript", subTitle);
//        ext.put("subTitle", subTitle);
        ext.put("sellingPrice", item.getString(AliPayFirstPageBuildItemVoSdkExtPt.SELLING_PRICE));
        ext.put("originPrice", item.getString(AliPayFirstPageBuildItemVoSdkExtPt.ORIGIN_PRICE));

        serviceContentRec.setExtMap(ext);

        return serviceContentRec;
    }

}
