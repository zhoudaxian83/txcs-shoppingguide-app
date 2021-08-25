package com.tmall.wireless.tac.biz.processor.brandclub.fp;

import com.alibaba.cola.dto.SingleResponse;
import com.google.common.collect.Lists;
import com.tmall.aselfcaptain.item.model.ChannelDataDO;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkContentService;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.store.spi.render.RenderSpi;
import com.tmall.wireless.tac.biz.processor.common.PackageNameKey;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.Validate.noNullElements;

@Service
public class BrandClubFirstPageHandler extends RpmReactiveHandler<SgFrameworkResponse<ContentVO>> {

    @Autowired
    ShoppingguideSdkContentService shoppingguideSdkContentService;


    @Autowired
    BrandContentSetIdService brandContentSetIdService;
    @Override
    public Flowable<TacResult<SgFrameworkResponse<ContentVO>>> executeFlowable(Context context) throws Exception {

        brandContentSetIdService.getGroupAndBrandMapping(Lists.newArrayList(3577479L));
        BizScenario b = BizScenario.valueOf(
                ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                ScenarioConstantApp.BRAND_CLUB_FP
        );

        b.addProducePackage(PackageNameKey.CONTENT_FEEDS);

        return shoppingguideSdkContentService.recommend(context, b).map(TacResult::newResult);
    }

}
