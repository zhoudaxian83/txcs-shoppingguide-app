package com.tmall.wireless.tac.biz.processor.alipay.service.impl;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.recmixer.common.service.facade.model.CategoryContentRet;
import com.alipay.recmixer.common.service.facade.model.MiddlePageRec;
import com.alipay.recmixer.common.service.facade.model.MixerCollectRecResult;
import com.alipay.recmixer.common.service.facade.model.ServiceContentRec;
import com.alipay.tradecsa.common.service.spi.request.MiddlePageFloorDTO;
import com.alipay.tradecsa.common.service.spi.request.MiddlePageSPIRequest;
import com.alipay.tradecsa.common.service.spi.request.PageFloorAtomicDTO;
import com.alipay.tradecsa.common.service.spi.request.PageFloorDetailDTO;
import com.alipay.tradecsa.common.service.spi.response.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.wireless.tac.biz.processor.alipay.constant.AliPayConstant;
import com.tmall.wireless.tac.biz.processor.alipay.service.IAliPayService;
import com.tmall.wireless.tac.biz.processor.alipay.service.ext.firstPage.AliPayFirstPageBuildItemVoSdkExtPt;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.AtomicCardProcessRequest;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.AtomicCardProcessorFactory;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.IAtomicCardProcessor;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.wzt.WuZheTianBuildItemVOExtPt;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tmall.tcls.gs.sdk.ext.extension.AsyncExtPt.LOGGER;
import static com.tmall.wireless.tac.biz.processor.alipay.service.ext.firstPage.AliPayItemUserCommonParamsBuildSdkExtPt.CONTEXT_KEY;

@Service("aliPayServiceImpl")
public class AliPayServiceImpl implements IAliPayService {

    Logger LOGGER = LoggerFactory.getLogger(AliPayServiceImpl.class);

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

        HadesLogUtil.stream("AliPayServiceImpl.processFirstPage")
                .kv("","").info();
        LOGGER.info("AliPayServiceImpl.processFirstPage");
        BizScenario bizScenario = BizScenario.valueOf(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                ScenarioConstantApp.SCENARIO_ALI_PAY_FIRST_PAGE);

        return shoppingguideSdkItemService.recommend(context, bizScenario)
                .map(re -> {
                    Object o = context.get(CONTEXT_KEY);
                    return convertMixerCollectRecResult(re, (GeneralItem) o);
                }).onErrorReturn(throwable -> {
                    LOGGER.error("shoppingguideSdkItemService.recommend error:", throwable);
                    MixerCollectRecResult mixerCollectRecResult = new MixerCollectRecResult();
                    mixerCollectRecResult.setSuccess(false);
                    mixerCollectRecResult.setErrorCode("convertMixerCollectRecResultError");
                    return mixerCollectRecResult;
                });

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
        List<ItemEntityVO> itemEntityVOS = re.getItemAndContentList().subList(0, Math.min(3, re.getItemAndContentList().size()));
        List<ServiceContentRec> collect = itemEntityVOS.stream().map(e -> convert(e, aldData)).collect(Collectors.toList());
        categoryContentRet.setServiceList(collect);
        categoryContentRet.setSuccess(true);
        LOGGER.info("mixerCollectRecResult:{}", JSON.toJSONString(mixerCollectRecResult));
        return mixerCollectRecResult;
    }

    @Override
    public Flowable<MiddlePageSPIResponse> processMiddlePage(Context context, MiddlePageSPIRequest middlePageSPIRequest) {




        BizScenario bizScenario = BizScenario.valueOf(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                ScenarioConstantApp.SCENARIO_ALI_PAY_MIDDLE_PAGE);


        return shoppingguideSdkItemService.recommend(context, bizScenario).map(re -> {

            Object o = context.get(CONTEXT_KEY);
            return convertMiddleResult(re, (GeneralItem)o, middlePageSPIRequest);
        }).onErrorReturn(throwable -> {
            LOGGER.error("convertMiddleResultError", throwable);
            MiddlePageSPIResponse middlePageSPIResponse = new MiddlePageSPIResponse();
            middlePageSPIResponse.setSuccess(false);
            middlePageSPIResponse.setErrorCode("convertMiddleResultError");
            return middlePageSPIResponse;
        });

    }

    private MiddlePageSPIResponse convertMiddleResult(SgFrameworkResponse<ItemEntityVO> re, GeneralItem aldData, MiddlePageSPIRequest middlePageSPIRequest) {


        MiddlePageSPIResponse middlePageSPIResponse = new MiddlePageSPIResponse();
        middlePageSPIResponse.setHasMore(true);

        // 头部区
        PageFloorHeaderDTO pageFloorHeaderDTO = new PageFloorHeaderDTO();
        pageFloorHeaderDTO.setType("image");
//        pageFloorHeaderDTO.setTitle(aldData.getString(headSubTitleAldKey));
        pageFloorHeaderDTO.setSubtitle("送货上门 品质保证");
        pageFloorHeaderDTO.setBgColor("#F8FDE4");
        pageFloorHeaderDTO.setFullMediaUrl("https://gw.alicdn.com/imgextra/i1/O1CN01264lUX1wNcKQUsoRT_!!6000000006296-0-tps-1125-492.jpg");
//        pageFloorHeaderDTO.setSubTitleImgUrl(aldData.getString(headBgPicAldKey));
        pageFloorHeaderDTO.setPromotion(true);
        pageFloorHeaderDTO.setType("text");
        middlePageSPIResponse.setPageFloorHeaderDTO(pageFloorHeaderDTO);



        // 导航栏
        PageFloorNavigationDTO pageFloorNavigationDTO = new PageFloorNavigationDTO();
        pageFloorNavigationDTO.setTitle("官方自营 极速送达");
        pageFloorNavigationDTO.setTitleImage("https://gw.alicdn.com/imgextra/i1/O1CN01FTjVSn1vkEGlrWJCR_!!6000000006210-2-tps-72-72.png");
//        pageFloorNavigationDTO.setTitleImage(aldData.getString(navigationIconPicAldKey));
        pageFloorNavigationDTO.setStyle("dark");
        pageFloorNavigationDTO.setTitleLightImageUrl("https://gw.alicdn.com/imgextra/i1/O1CN01FTjVSn1vkEGlrWJCR_!!6000000006210-2-tps-72-72.png");
        middlePageSPIResponse.setPageFloorNavigationDTO(pageFloorNavigationDTO);


        List<ItemEntityVO> itemAndContentList = re.getItemAndContentList();
        List<PageFloorResultDTO> floorResultDTOS = Lists.newArrayList();
        middlePageSPIRequest.getMiddlePageFloorDTOList().forEach(middlePageFloorDTO -> {


            PageFloorResultDTO pageFloorResultDTO = new PageFloorResultDTO();
            pageFloorResultDTO.setPageFloorId(middlePageFloorDTO.getPageFloorId());

            List<PageFloorAtomicDTO> pageFloorAtomicDTOS = Optional.of(middlePageFloorDTO).map(MiddlePageFloorDTO::getPageFloorDetailDTO).map(PageFloorDetailDTO::getPageFloorAtomicDTOList).orElse(Lists.newArrayList());
            List<PageFloorAtomicResultDTO> pageFloorAtomicResultDTOList = pageFloorAtomicDTOS.stream()
                    .map(pageFloorAtomicDTO -> {
                        AtomicCardProcessRequest atomicCardProcessRequest = new AtomicCardProcessRequest();
                        atomicCardProcessRequest.setPageFloorAtomicDTO(pageFloorAtomicDTO);
                        atomicCardProcessRequest.setAldData(aldData);
                        atomicCardProcessRequest.setItemAndContentList(itemAndContentList);
                        return processAtomic(atomicCardProcessRequest);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            PageFloorResultDetailDTO pageFloorResultDetailDTO = new PageFloorResultDetailDTO();
            pageFloorResultDetailDTO.setPageFloorAtomicResultDTOList(pageFloorAtomicResultDTOList);
            pageFloorResultDTO.setPageFloorResultDetailDTO(pageFloorResultDetailDTO);

            floorResultDTOS.add(pageFloorResultDTO);

        });

        middlePageSPIResponse.setPageFloorResultDTOList(floorResultDTOS);

        LOGGER.warn("middlePageSPIResponse:{}", JSONObject.toJSONString(middlePageSPIResponse));
        return middlePageSPIResponse;
    }

    private PageFloorAtomicResultDTO processAtomic(AtomicCardProcessRequest atomicCardProcessRequest) {

        String atomicId = Optional.of(atomicCardProcessRequest).map(AtomicCardProcessRequest::getPageFloorAtomicDTO).map(PageFloorAtomicDTO::getAtomCardTemplateId).orElse("");

        if (StringUtils.isEmpty(atomicId)) {
            return null;
        }

        IAtomicCardProcessor processorByFloorId = atomicCardProcessorFactory.getProcessorByFloorId(atomicId);
        if (processorByFloorId == null) {
            return null;
        }

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
        serviceContentRec.setItemType("SSU");
        String subTitle = item.getString(AliPayFirstPageBuildItemVoSdkExtPt.PROMOTION_POINT);
        subTitle = StringUtils.isEmpty(subTitle) ? aldData.getString(itemLabelAldKey) : subTitle;
        Map<String, String> ext = Maps.newHashMap();
        ext.put("subScript", subTitle);
        ext.put("sellingPrice", item.getString(AliPayFirstPageBuildItemVoSdkExtPt.SELLING_PRICE));
        ext.put("originPrice", item.getString(AliPayFirstPageBuildItemVoSdkExtPt.ORIGIN_PRICE));

        serviceContentRec.setExtMap(ext);
        MiddlePageRec middlePageRec = new MiddlePageRec();

        Map<String, String> extInfoMap = Maps.newHashMap();
        extInfoMap.put("topItemIds", String.valueOf(item.getItemId()));
        middlePageRec.setItemParamMap(extInfoMap);
        serviceContentRec.setMiddlePageRec(middlePageRec);
        return serviceContentRec;
    }

}
