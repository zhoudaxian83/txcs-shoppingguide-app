package com.tmall.wireless.tac.biz.processor.icon.level2;

import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkContentService;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.model.IconTabDTO;
import com.tmall.wireless.tac.client.domain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Level2RecommendService {

    @Autowired
    ShoppingguideSdkContentService shoppingguideSdkContentService;

    public static String level2Request = "level2Request";

    List<IconTabDTO> recommend(Level2Request level2Request, Context context) {
        BizScenario b = BizScenario.valueOf(
                ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                ScenarioConstantApp.ICON_CONTENT_LEVEL2
        );

        context.put("level2Request", level2Request);
        shoppingguideSdkContentService.recommend(context, b);
    }

}
