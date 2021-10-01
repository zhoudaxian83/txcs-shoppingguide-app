package com.tmall.wireless.tac.biz.processor.todayCrazyTab;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.contentinfo.ContentInfoQuerySdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentInfoDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import io.reactivex.Flowable;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.Opt;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created from template by 进舟 on 2021-09-22 16:00:05.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "todayCrazyTab"
)
public class TodayCrazyTabContentInfoQuerySdkExtPt extends Register implements ContentInfoQuerySdkExtPt {

    @Override
    public Flowable<Response<Map<Long, ContentInfoDTO>>> process(SgFrameworkContextContent sgFrameworkContextContent) {
        return Flowable.just(Response.success(getContentInfo(sgFrameworkContextContent)));
    }

    private Map<Long, ContentInfoDTO> getContentInfo(SgFrameworkContextContent sgFrameworkContextContent) {

        List<ContentEntity> contentEntityList = Optional.of(sgFrameworkContextContent)
                .map(SgFrameworkContextContent::getContentEntityOriginDataDTO)
                .map(OriginDataDTO::getResult)
                .orElse(Lists.newArrayList());

        return contentEntityList.stream()
                .collect(Collectors.toMap(ContentEntity::getContentId, contentEntity -> {
                    Map<String, Object> extInfo = contentEntity.getExtInfo();
                    ContentInfoDTO contentInfoDTO = new ContentInfoDTO();
                    contentInfoDTO.setContentInfo(extInfo);
                    return contentInfoDTO;
                }));

    }
}
