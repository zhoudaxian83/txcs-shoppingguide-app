package com.tmall.wireless.tac.biz.processor.icon.level3.ext;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.aself.shoppingguide.client.loc.domain.AddressDTO;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.aselfcommon.model.column.MainColumnDTO;
import com.tmall.aselfcommon.model.column.MaterialDTO;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.contentinfo.ContentInfoQuerySdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.constant.RequestKeyConstant;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.txcs.gs.spi.recommend.AldSpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.ColumnCacheService;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2RecommendService;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2Request;
import com.tmall.wireless.tac.biz.processor.icon.level3.Level3RecommendService;
import com.tmall.wireless.tac.biz.processor.icon.level3.Level3Request;
import io.reactivex.Flowable;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import com.alibaba.aladdin.lamp.domain.request.Request;
import com.alibaba.aladdin.lamp.domain.request.RequestItem;
import com.alibaba.aladdin.lamp.domain.request.modules.LocationInfo;
import com.alibaba.aladdin.lamp.domain.response.ResResponse;
import com.alibaba.fastjson.JSONObject;

@Service
@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.ICON_CONTENT_LEVEL3
)
public class IconLevel3ContentInfoQuerySdkExtPt extends Register implements ContentInfoQuerySdkExtPt {
    Logger LOGGER = LoggerFactory.getLogger(IconLevel3ContentInfoQuerySdkExtPt.class);

    public static final String SUB_COLUMN_DTO_KEY = "SUB_COLUMN_DTO_KEY";


    private static final String APP_NAME = "txcs-shoppingguide-app";

    public static final String yxsdPrefix = "SG_TMCS_1H_DS:";

    public static final String brdPrefix = "SG_TMCS_HALF_DAY_DS:";

    @Autowired
    private AldSpi aldSpi;

    @Autowired
    ColumnCacheService columnCacheService;
    @Override
    public Flowable<Response<Map<Long, ContentInfoDTO>>> process(SgFrameworkContextContent sgFrameworkContextContent) {

        Map<Long, ContentInfoDTO> contentInfoDTOMap = processContentMap(sgFrameworkContextContent);
        Level3Request level3Request =(Level3Request) Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getTacContext).map(c -> c.get(Level3RecommendService.level3RequestKey)).orElse(null);
        Preconditions.checkArgument(level3Request != null);
        if (!StringUtils.isNumeric(level3Request.getLevel2Id())) {
            return Flowable.just(Response.success(contentInfoDTOMap));
        }
        Long mockId = 24563L;
        MainColumnDTO column = columnCacheService.getColumn(mockId/*Long.parseLong(level2Request.getLevel1Id())*/);
        MaterialDTO materialDTO = column.getMaterialDTOMap().get("bannerAppId");
        if (materialDTO == null || StringUtils.isBlank(materialDTO.getExtValue())) {
            return Flowable.just(Response.success(contentInfoDTOMap));
        }
        if (StringUtils.isBlank((materialDTO.getExtValue()))) {
            return Flowable.just(Response.success(contentInfoDTOMap));
        }
        String resId = materialDTO.getExtValue();
        Object resResponse = getAldStaticDataByResourceId(resId, sgFrameworkContextContent);
        if (resResponse == null) {
            return Flowable.just(Response.success(contentInfoDTOMap));
        }

        return Flowable.just(Response.success(contentInfoDTOMap));
    }

    private Map<Long, ContentInfoDTO> processContentMap(SgFrameworkContextContent sgFrameworkContextContent) {
        Level3Request level3Request =(Level3Request) Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getTacContext).map(c -> c.get(Level3RecommendService.level3RequestKey)).orElse(null);
        Preconditions.checkArgument(level3Request != null);

        OriginDataDTO<ContentEntity> contentEntityOriginDataDTO = sgFrameworkContextContent.getContentEntityOriginDataDTO();
        List<ContentEntity> result = contentEntityOriginDataDTO.getResult();

        MainColumnDTO column = getMainColumn(level3Request);
        if (column == null || MapUtils.isEmpty(column.getSubColumnDTOMap())) {
            return Maps.newHashMap();
        }
        Map<Long, ContentInfoDTO> contentInfoDTOMap = Maps.newHashMap();
        for (ContentEntity contentEntity : result) {
            if (column.getSubColumnDTOMap().get(contentEntity.getContentId()) != null) {
                ContentInfoDTO contentInfoDTO = new ContentInfoDTO();
                Map<String, Object> contentInfo = Maps.newHashMap();
                contentInfo.put(SUB_COLUMN_DTO_KEY, column.getSubColumnDTOMap().get(contentEntity.getContentId()));
                contentInfoDTO.setContentInfo(contentInfo);
                contentInfoDTOMap.put(contentEntity.getContentId(), contentInfoDTO);
            }
        }


        return contentInfoDTOMap;
    }

    private MainColumnDTO getMainColumn(Level3Request level3Request) {
        return columnCacheService.getColumn(Long.valueOf(level3Request.getLevel2Id()));
    }



    private Object getAldStaticDataByResourceId(String resourceId, SgFrameworkContextContent sgFrameworkContextContent){
        Request request = buildAldRequest(resourceId, sgFrameworkContextContent);
        if (request == null) {
            return null;
        }
        Map<String, ResResponse> aldResponseMap = aldSpi.queryAldInfoSync(request);
        if(org.apache.commons.collections.MapUtils.isNotEmpty(aldResponseMap)){
            ResResponse resResponse = aldResponseMap.get(resourceId);
            Object data = resResponse.getData();
            return data;
        }
        return null;
    }

    private Request buildAldRequest(String resourceId, SgFrameworkContextContent contextContent) {
        Request request = new Request();

        request.setCallSource(APP_NAME);
        RequestItem item = new RequestItem();
        item.setCount(50);
        item.setResId(resourceId);
        JSONObject data = new JSONObject();
        item.setData(data);
        request.setRequestItems(Lists.newArrayList(item));
        //地址信息
        LocationInfo locationInfo = request.getLocationInfo();
        //四级地址

        AddressDTO addressDto;
        String csa = (String)contextContent.getRequestParams().getOrDefault(RequestKeyConstant.USER_PARAMS_KEY_CSA, "");
        if (StringUtils.isBlank(csa)) {
            return null;
        }
        if(StringUtils.isNotEmpty(csa)){
            addressDto = AddressUtil.parseCSA(csa);
            locationInfo.setCityLevel4(String.valueOf(addressDto.getDistrictId()));
            List<String> wdkCodes = Lists.newArrayList();
            if (addressDto.isRt1HourStoreCover()) {
                wdkCodes.add(yxsdPrefix + addressDto.getRt1HourStoreId());
            } else if(addressDto.isRtHalfDayStoreCover()){
                wdkCodes.add(brdPrefix + addressDto.getRtHalfDayStoreId());
            }
            locationInfo.setWdkCodes(wdkCodes);
        }
        Long userId = Optional.of(contextContent).
            map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO)
            .map(UserDO::getUserId).orElse(0L);
        request.getUserProfile().setUserId(userId);

        return request;
    }




}
