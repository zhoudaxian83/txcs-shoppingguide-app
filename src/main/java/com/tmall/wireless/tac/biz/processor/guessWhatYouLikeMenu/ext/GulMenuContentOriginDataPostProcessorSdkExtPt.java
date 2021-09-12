package com.tmall.wireless.tac.biz.processor.guessWhatYouLikeMenu.ext;

import com.google.common.base.Splitter;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataPostProcessorSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author Yushan
 * @date 2021/9/10 5:10 下午
 */
@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.CNXH_MENU_FEEDS
)
public class GulMenuContentOriginDataPostProcessorSdkExtPt extends Register implements ContentOriginDataPostProcessorSdkExtPt {
    @Override
    public OriginDataDTO<ContentEntity> process(ContentOriginDataProcessRequest contentOriginDataProcessRequest) {

        // 菜谱置顶坑位
        SgFrameworkContextContent sgFrameworkContextContent = contentOriginDataProcessRequest.getSgFrameworkContextContent();
        OriginDataDTO<ContentEntity> contentEntityOriginDataDTO = contentOriginDataProcessRequest.getContentEntityOriginDataDTO();
        // 非首页直接返回结果
        if (sgFrameworkContextContent.getCommonUserParams().getUserPageInfo().getIndex() != 0) {
            return contentEntityOriginDataDTO;
        }
        String topContentIdListString = (String)sgFrameworkContextContent.getTacContext().getParams().get("topContentIdList");
        List<String> topContentIdList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(topContentIdListString);
        for (int i = 0; i < topContentIdList.size(); i++) {
            Long topContentId = Long.getLong(topContentIdList.get(i));
            for (int j = 0; j < contentEntityOriginDataDTO.getResult().size(); j++) {
                ContentEntity contentEntity = contentEntityOriginDataDTO.getResult().get(j);
                if (contentEntity.getContentId().equals(topContentId)) {
                    Collections.swap(contentEntityOriginDataDTO.getResult(), i, j);
                }
            }
        }
        return contentEntityOriginDataDTO;
    }
}
