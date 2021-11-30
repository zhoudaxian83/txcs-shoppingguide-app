package com.tmall.wireless.tac.biz.processor.icon.level2.ext;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.tmall.aselfcommon.model.column.MainColumnDTO;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.contentinfo.ContentInfoQuerySdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.ColumnCacheService;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2RecommendService;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2Request;
import io.reactivex.Flowable;
import org.apache.commons.collections4.MapUtils;
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
        , scenario = ScenarioConstantApp.ICON_CONTENT_LEVEL2
)
public class IconLevel2ContentInfoQuerySdkExtPt extends Register implements ContentInfoQuerySdkExtPt {
    Logger LOGGER = LoggerFactory.getLogger(IconLevel2ContentInfoQuerySdkExtPt.class);

    public static final String MainColumnDTOKey = "MainColumnDTOKey";

    @Autowired
    ColumnCacheService columnCacheService;
    @Override
    public Flowable<Response<Map<Long, ContentInfoDTO>>> process(SgFrameworkContextContent sgFrameworkContextContent) {
        Level2Request level2Request =(Level2Request) Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getTacContext).map(c -> c.get(Level2RecommendService.level2RequestKey)).orElse(null);
        Preconditions.checkArgument(level2Request != null);

        OriginDataDTO<ContentEntity> contentEntityOriginDataDTO = sgFrameworkContextContent.getContentEntityOriginDataDTO();
        List<ContentEntity> result = contentEntityOriginDataDTO.getResult();
        Map<Long, MainColumnDTO> mainColumnMap = columnCacheService.getMainColumnMap(level2Request.getLevel1Id());
        Map<Long, ContentInfoDTO> contentInfoDTOMap = Maps.newHashMap();
        for (ContentEntity contentEntity : result) {
            if (mainColumnMap.get(contentEntity.getContentId()) != null) {
                ContentInfoDTO contentInfoDTO = new ContentInfoDTO();
                Map<String, Object> contentInfo = Maps.newHashMap();
                contentInfo.put(MainColumnDTOKey, mainColumnMap.get(contentEntity.getContentId()));
                contentInfoDTO.setContentInfo(contentInfo);
                contentInfoDTOMap.put(contentEntity.getContentId(), contentInfoDTO);
            }
        }
        return Flowable.just(Response.success(contentInfoDTOMap));
    }



}
