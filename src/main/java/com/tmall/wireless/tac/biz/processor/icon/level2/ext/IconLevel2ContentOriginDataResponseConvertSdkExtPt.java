package com.tmall.wireless.tac.biz.processor.icon.level2.ext;

import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataResponseConvertSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentResponseConvertRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.ContentEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.OriginDataDTO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C
        , scenario = ScenarioConstantApp.ICON_CONTENT_LEVEL2
)
public class IconLevel2ContentOriginDataResponseConvertSdkExtPt extends Register implements ContentOriginDataResponseConvertSdkExtPt {


    @Override
    public OriginDataDTO<ContentEntity> process(ContentResponseConvertRequest contentResponseConvertRequest) {
        OriginDataDTO<ContentEntity> contentEntityOriginDataDTO = new OriginDataDTO<>();
        String response = contentResponseConvertRequest.getResponse();

        if (StringUtils.isNotEmpty(response)) {
            contentEntityOriginDataDTO.setErrorMsg("tpp response is empty");
            contentEntityOriginDataDTO.setErrorCode("TPP_RETURN_EMPTY");
            return contentEntityOriginDataDTO;
        }

        return null;
    }
}
