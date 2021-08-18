package com.tmall.wireless.tac.biz.processor.alipay.service.impl;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSONObject;
import com.alipay.recmixer.common.service.facade.model.CategoryContentRet;
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
import com.tmall.wireless.tac.biz.processor.alipay.service.ext.firstPage.AliPayFirstPageBuildItemVoSdkExtPt;
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

import static com.tmall.tcls.gs.sdk.ext.extension.AsyncExtPt.LOGGER;
import static com.tmall.wireless.tac.biz.processor.alipay.service.ext.firstPage.AliPayItemUserCommonParamsBuildSdkExtPt.CONTEXT_KEY;

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
                .map(re -> {
                    Object o = context.get(CONTEXT_KEY);
                    return convertMixerCollectRecResult(re, (GeneralItem) o);
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
        });

    }

    private MiddlePageSPIResponse convertMiddleResult(SgFrameworkResponse<ItemEntityVO> re, GeneralItem aldData, MiddlePageSPIRequest middlePageSPIRequest) {


        MiddlePageSPIResponse middlePageSPIResponse = new MiddlePageSPIResponse();
        middlePageSPIResponse.setHasMore(true);

        // 头部区
        PageFloorHeaderDTO pageFloorHeaderDTO = new PageFloorHeaderDTO();
        pageFloorHeaderDTO.setType("image");
        pageFloorHeaderDTO.setTitle(aldData.getString(headSubTitleAldKey));
        pageFloorHeaderDTO.setSubtitle(aldData.getString(headSubTitleAldKey));
        pageFloorHeaderDTO.setBgColor(aldData.getString(headColorAldKey));
        pageFloorHeaderDTO.setSubTitleImgUrl(aldData.getString(headBgPicAldKey));
        middlePageSPIResponse.setPageFloorHeaderDTO(pageFloorHeaderDTO);

        // 导航栏
        PageFloorNavigationDTO pageFloorNavigationDTO = new PageFloorNavigationDTO();
        pageFloorNavigationDTO.setTitle(aldData.getString(navigationTitleAldKey));
        pageFloorNavigationDTO.setTitleImage(aldData.getString(navigationIconPicAldKey));
        pageFloorNavigationDTO.setStyle("light");
        pageFloorNavigationDTO.setTitleLightImageUrl(aldData.getString(navigationIconPicAldKey));
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

        String subTitle = item.getString(AliPayFirstPageBuildItemVoSdkExtPt.PROMOTION_POINT);
        subTitle = StringUtils.isEmpty(subTitle) ? aldData.getString(itemLabelAldKey) : subTitle;
        Map<String, String> ext = Maps.newHashMap();
        ext.put("subScript", subTitle);
        ext.put("sellingPrice", item.getString(AliPayFirstPageBuildItemVoSdkExtPt.SELLING_PRICE));
        ext.put("originPrice", item.getString(AliPayFirstPageBuildItemVoSdkExtPt.ORIGIN_PRICE));

        serviceContentRec.setExtMap(ext);

        return serviceContentRec;
    }

}
