package com.tmall.wireless.tac.biz.processor.guessYourLikeShopCart4;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.biz.extensions.item.filter.DefaultItemFilterSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemFilterRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemFilterSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created from template by 程斐斐 on 2021-09-29 17:21:34.
 */

@SdkExtension(
        bizId = "supermarket",
        useCase = "b2c",
        scenario = "guessYourLikeShopCart4"
)
public class GuessYourLikeShopCart4ItemFilterSdkExtPt extends DefaultItemFilterSdkExtPt implements ItemFilterSdkExtPt {

    @Autowired
    TacLoggerImpl tacLogger;
    @Override
    public SgFrameworkResponse<ItemEntityVO> process(ItemFilterRequest itemFilterRequest) {

        tacLogger.info("商品过滤处理：itemFilterRequest="+ JSON.toJSONString(itemFilterRequest));
        //SgFrameworkResponse<ItemEntityVO> process = super.process(itemFilterRequest);
        //tacLogger.info("商品过滤处理：ItemEntityVOResponse="+ JSON.toJSONString(process));
        //return process;
        try {
            SgFrameworkResponse<ItemEntityVO> entityVOSgFrameworkResponse = itemFilterRequest.getEntityVOSgFrameworkResponse();
            List<ItemEntityVO> itemAndContentList = entityVOSgFrameworkResponse.getItemAndContentList();
            if (CollectionUtils.isEmpty(itemAndContentList)) {
                tacLogger.info("itemAndContentList is null");
                return entityVOSgFrameworkResponse;
            } else {
                List<ItemEntityVO> itemAndContentListAfterFilter = Lists.newArrayList();
                Iterator var5 = itemAndContentList.iterator();

                while(var5.hasNext()) {
                    ItemEntityVO entityVO = (ItemEntityVO)var5.next();
                    if (entityVO != null) {
                        if (!this.canBuy(entityVO)) {
                            tacLogger.info("ITEM_FILTER_BY_CAN_BUY");
                        } else if (this.checkField(entityVO)) {
                            itemAndContentListAfterFilter.add(entityVO);
                        }
                    }
                }

                entityVOSgFrameworkResponse.setItemAndContentList(itemAndContentListAfterFilter);
                tacLogger.info("过滤完成");
                return entityVOSgFrameworkResponse;
            }
        } catch (Exception e) {
            tacLogger.info("过滤出现异常");
            return null;
        }
    }
    protected boolean checkField(ItemEntityVO entityVO) {
        List<String> checkField = this.getFieldList();
        if (CollectionUtils.isEmpty(checkField)) {
            return true;
        } else {
            Iterator var3 = checkField.iterator();

            String field;
            do {
                if (!var3.hasNext()) {
                    return true;
                }

                field = (String)var3.next();
            } while(!Objects.isNull(entityVO.get(field)));

            LOGGER.error("itemFilter,{}, itemId:{},field:{}", new Object[]{"ITEM_FILTER_BY_FIELD_ERROR", entityVO.getString("itemId"), field});
            return false;
        }
    }

    protected List<String> getFieldList() {
        return CHECK_FIELD;
    }

    private boolean canBuy(ItemEntityVO item) {
        Boolean canBuy = item.getBoolean("canBuy");
        Boolean sellOut = item.getBoolean("sellOut");
        return (canBuy == null || canBuy) && (sellOut == null || !sellOut);
    }
}
