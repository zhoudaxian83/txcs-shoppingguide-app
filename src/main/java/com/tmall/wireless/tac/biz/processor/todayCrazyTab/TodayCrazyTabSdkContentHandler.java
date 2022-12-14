package com.tmall.wireless.tac.biz.processor.todayCrazyTab;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkContentService;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.common.PackageNameKey;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.biz.processor.huichang.service.HallCommonContentRequestProxy;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created from template by 进舟 on 2021-09-22 11:17:10.
 *
 */

@Service
public class TodayCrazyTabSdkContentHandler extends TacReactiveHandler4Ald {

    @Autowired
    HallCommonContentRequestProxy hallCommonContentRequestProxy;
    @Autowired
    ShoppingguideSdkContentService shoppingguideSdkContentService;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald context) throws Exception {
        BizScenario b = BizScenario.valueOf(
                "supermarket",
                "b2c",
                "todayCrazyTab"
        );

        b.addProducePackage(HallScenarioConstant.HALL_CONTENT_SDK_PACKAGE);
        b.addProducePackage(PackageNameKey.OLD_RECOMMEND);

        return shoppingguideSdkContentService.recommend(context, b)
                .map(response -> {
                    List<GeneralItem> re = Lists.newArrayList();
                    re.addAll(convertAldItem(response));
                    return re;
                })
                .map(TacResult::newResult)
                .onErrorReturn(r -> TacResult.errorResult(""));
    }

    public List<GeneralItem> convertAldItem(SgFrameworkResponse<ContentVO> response) {
        return response.getItemAndContentList().stream().map(contentVO -> {
            GeneralItem generalItem = new GeneralItem();
            contentVO.keySet().forEach(key -> {
                generalItem.putIfAbsent(key, contentVO.get(key));
            });
            return generalItem;
        }).collect(Collectors.toList());
    }
}
