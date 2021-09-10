package com.tmall.wireless.tac.biz.processor.icon.item;


import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.service.ShoppingguideSdkItemService;
import com.tmall.wireless.tac.biz.processor.common.PackageNameKey;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemRecommendService {

    public static final String ITEM_REQUEST_KEY = "itemRequestKey";
    @Autowired
    ShoppingguideSdkItemService shoppingguideSdkItemService;
    Logger LOGGER = LoggerFactory.getLogger(ItemRecommendService.class);

    public Flowable<SgFrameworkResponse<ItemEntityVO>> recommend(ItemRequest itemRequest, Context context) {

        SgFrameworkResponse<ItemEntityVO> itemRecommendErrorResponse = new SgFrameworkResponse<>();
        itemRecommendErrorResponse.setSuccess(false);
        itemRecommendErrorResponse.setErrorMsg("shoppingguideSdkItemService.recommendWitchContext defaultEmpty");
        BizScenario b = BizScenario.valueOf(
                ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
                ScenarioConstantApp.LOC_TYPE_B2C,
                ScenarioConstantApp.ICON_ITEM
        );
        b.addProducePackage(PackageNameKey.OLD_RECOMMEND);
        context.put(ITEM_REQUEST_KEY, itemRequest);
        return shoppingguideSdkItemService.recommendWitchContext(context, b)
                .map(contentContext -> {
                    if (contentContext.getEntityVOSgFrameworkResponse() == null) {
                        LOGGER.error("shoppingguideSdkItemService.recommendWitchContext returnEmpty");

                    }
                    return contentContext.getEntityVOSgFrameworkResponse();
//                    if (contentContext.getEntityVOSgFrameworkResponse() != null) {
//                        return contentContext.getEntityVOSgFrameworkResponse();
//                    }
//
//                    itemRecommendErrorResponse.setErrorMsg("shoppingguideSdkItemService.recommendWitchContext returnEmpty");
//                    return itemRecommendErrorResponse;

                }).defaultIfEmpty(itemRecommendErrorResponse);
    }

}
