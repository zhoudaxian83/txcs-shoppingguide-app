package com.tmall.wireless.tac.biz.processor.icon.level3.ext;

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
import com.tmall.wireless.tac.biz.processor.icon.level3.Level3RecommendService;
import com.tmall.wireless.tac.biz.processor.icon.level3.Level3Request;
import io.reactivex.Flowable;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.ICON_CONTENT_LEVEL3
)
public class IconLevel3ContentInfoQuerySdkExtPt extends Register implements ContentInfoQuerySdkExtPt {
    Logger LOGGER = LoggerFactory.getLogger(IconLevel3ContentInfoQuerySdkExtPt.class);

    public static final String SUB_COLUMN_DTO_KEY = "SUB_COLUMN_DTO_KEY";

    @Autowired
    ColumnCacheService columnCacheService;
    @Override
    public Flowable<Response<Map<Long, ContentInfoDTO>>> process(SgFrameworkContextContent sgFrameworkContextContent) {

        Map<Long, ContentInfoDTO> contentInfoDTOMap = processContentMap(sgFrameworkContextContent);

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


}
