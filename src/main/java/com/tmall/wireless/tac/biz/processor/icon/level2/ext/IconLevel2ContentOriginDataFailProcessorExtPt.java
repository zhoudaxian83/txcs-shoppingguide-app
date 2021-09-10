package com.tmall.wireless.tac.biz.processor.icon.level2.ext;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.tmall.aselfcommon.model.column.MainColumnDTO;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataFailProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.ColumnCacheService;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2RecommendService;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2Request;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.ICON_CONTENT_LEVEL2
)
public class IconLevel2ContentOriginDataFailProcessorExtPt extends Register implements ContentOriginDataFailProcessorSdkExtPt {
    @Autowired
    ColumnCacheService columnCacheService;
    @Override
    public OriginDataDTO<ContentEntity> process(ContentOriginDataProcessRequest contentOriginDataProcessRequest) {
        Level2Request level2Request =(Level2Request) Optional.of(contentOriginDataProcessRequest)
                .map(ContentOriginDataProcessRequest::getSgFrameworkContextContent)
                .map(SgFrameworkContext::getTacContext)
                .map(c -> c.get(Level2RecommendService.level2RequestKey))
                .orElse(null);

        Preconditions.checkArgument(level2Request != null);

        Map<Long, MainColumnDTO> mainColumnMap = columnCacheService.getMainColumnMap(level2Request.getLevel1Id());

        int i = 1;
        List<ContentEntity> contentEntityList = Lists.newArrayList();
        for (MainColumnDTO mainColumnDTO : mainColumnMap.values()) {

            ContentEntity contentEntity = new ContentEntity();
            contentEntity.setContentId(mainColumnDTO.getId());
            contentEntity.setItems(Lists.newArrayList());
            contentEntity.setRn(i++);
            contentEntity.setTrack_point("tpp.error");
            contentEntityList.add(contentEntity);
        }
        OriginDataDTO<ContentEntity> result = new OriginDataDTO<>();
        result.setResult(contentEntityList);
        result.setHasMore(false);
        result.setScm("tpp.error");
        result.setSuccess(true);
        return result;

    }
}
