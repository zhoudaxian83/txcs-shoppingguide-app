package com.tmall.wireless.tac.biz.processor.brandclub.bangdan;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataPostProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.domain.Context;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.BRAND_CLUB_BANGDAN
)
public class BrandClubBangdanContentOriginDataPostProcessorSdkExtPt extends Register implements ContentOriginDataPostProcessorSdkExtPt {
    @Override
    public OriginDataDTO<ContentEntity> process(ContentOriginDataProcessRequest contentOriginDataProcessRequest) {


        OriginDataDTO<ContentEntity> contentEntityOriginDataDTO = contentOriginDataProcessRequest.getContentEntityOriginDataDTO();
        Long topContentId = Optional.of(contentOriginDataProcessRequest)
                .map(ContentOriginDataProcessRequest::getSgFrameworkContextContent)
                .map(SgFrameworkContext::getTacContext)
                .map(Context::getParams)
                .map(m -> m.get("topContentIdList"))
                .map(Object::toString).filter(StringUtils::isNumeric).map(Long::valueOf).orElse(0L);

        if (topContentId <= 0) {
            return contentEntityOriginDataDTO;
        }

        List<ContentEntity> contentEntityList = Optional.of(contentOriginDataProcessRequest)
                .map(ContentOriginDataProcessRequest::getContentEntityOriginDataDTO)
                .map(OriginDataDTO::getResult)
                .orElse(Lists.newArrayList());

        if (CollectionUtils.isEmpty(contentEntityList)) {
            return contentEntityOriginDataDTO;
        }
        List<ContentEntity> topContentList = contentEntityList.stream()
                .filter(contentEntity -> contentEntity.getContentId().equals(topContentId)).collect(Collectors.toList());
        List<ContentEntity> notTopContentList = contentEntityList.stream()
                .filter(contentEntity -> !contentEntity.getContentId().equals(topContentId)).collect(Collectors.toList());
        List<ContentEntity> result = Lists.newArrayList();
        result.addAll(topContentList);
        result.addAll(notTopContentList);
        contentEntityOriginDataDTO.setResult(result);
        return contentEntityOriginDataDTO;
    }

}
