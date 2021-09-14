package com.tmall.wireless.tac.biz.processor.detail.o2o.item;

import javax.annotation.Resource;

import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.biz.processor.detail.common.convert.DetailConverterFactory;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecContentResultVO;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.stereotype.Service;

/**
 * @author: guichen
 * @Data: 2021/9/13
 * @Description:
 */
@Service
public class O2ODetailRecSdkItemHandler extends RpmReactiveHandler<DetailRecContentResultVO> {

    @Resource
    ShoppingguideSdkItemService shoppingguideSdkItemService;
    @Override
    public Flowable<TacResult<DetailRecContentResultVO>> executeFlowable(Context context) throws Exception {

        BizScenario bizScenario = BizScenario.valueOf(
            DetailConstant.BIZ_ID,
            DetailConstant.USE_CASE_O2O,
            DetailConstant.ITEM_SCENERIO
        );

        return shoppingguideSdkItemService.recommend(context, bizScenario)
            .map(response->{
                String recType = (String)context.getParams().get("recType");
                return DetailConverterFactory.instance.getConverter(recType).convert(response);
            })
            .map(TacResult::newResult);

    }
}

