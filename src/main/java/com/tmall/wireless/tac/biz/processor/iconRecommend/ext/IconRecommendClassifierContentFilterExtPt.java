package com.tmall.wireless.tac.biz.processor.iconRecommend.ext;

import com.alibaba.cola.extension.Extension;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.gs.framework.extensions.filter.ContentFilterExtPt;
import com.tmall.txcs.gs.framework.extensions.filter.ContentFilterRequest;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.txcs.gs.framework.model.ItemEntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.framework.support.LogUtil;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


/**
 * @author Yushan
 * @date 2021/9/10 2:21 下午
 */
@Extension(bizId = ScenarioConstant.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstant.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENARIO_ICON_RECOMMEND_CLASSIFIER
)
@Service
public class IconRecommendClassifierContentFilterExtPt implements ContentFilterExtPt {

    @Resource
    TacLogger logger;

    public static final int CONTENT_ITEM_SIZE = 6;
    @Override
    public SgFrameworkResponse<ContentVO> process(ContentFilterRequest contentFilterRequest) {
        try {
            SgFrameworkResponse<ContentVO> contentVOSgFrameworkResponse = contentFilterRequest.getContentVOSgFrameworkResponse();
            if (CollectionUtils.isEmpty(contentVOSgFrameworkResponse.getItemAndContentList())) {
                return contentVOSgFrameworkResponse;
            }

            Map<Long, List<ItemEntityVO>> contentId2CanBuyItemList = calcuContentId2CanBuyItemList(
                    contentVOSgFrameworkResponse.getItemAndContentList(),
                    contentFilterRequest.getSgFrameworkContextContent());

            List<ContentVO> contentListAfterFilter = Lists.newArrayList();

            contentVOSgFrameworkResponse.getItemAndContentList().forEach(contentVO -> {
                Long contentId = contentVO.getLong("contentId");
                if (contentId2CanBuyItemList.get(contentId) != null
                        && contentId2CanBuyItemList.get(contentId).size() >= CONTENT_ITEM_SIZE) {
                    contentListAfterFilter.add(contentVO);
                    contentVO.put("items", contentId2CanBuyItemList.get(contentId));
                } else {

                    LogUtil.errorCode(contentFilterRequest.getSgFrameworkContextContent().getBizScenario().getUniqueIdentity(),
                            "CONTENT_FILTER_BY_ITEM" + "," + contentId + "");
                }
            });

            if (!CollectionUtils.isEmpty(contentListAfterFilter)) {
                contentVOSgFrameworkResponse.setItemAndContentList(contentListAfterFilter);
            } else {
                LogUtil.errorCode(contentFilterRequest.getSgFrameworkContextContent().getBizScenario().getUniqueIdentity(),
                        "CONTENT_FILTER_RESULT_EMPTY",
                        "");
            }
            return contentVOSgFrameworkResponse;

        } catch (Exception e) {
            logger.error("IconRecommendClassifierContentFilterExtPt_error:", e);
        }
        return contentFilterRequest.getContentVOSgFrameworkResponse();
    }

    private Map<Long, List<ItemEntityVO>> calcuContentId2CanBuyItemList(List<ContentVO> contentVOS, SgFrameworkContextContent sgFrameworkContextContent) {

        Map<Long, List<ItemEntityVO>> result = Maps.newHashMap();

        contentVOS.stream().forEach(contentVO -> {

            List<ItemEntityVO> canBuyItemList = Lists.newArrayList();
            Long contentId = contentVO.getLong("contentId");
            result.put(contentId, canBuyItemList);
            List<ItemEntityVO> items = (List<ItemEntityVO>) contentVO.get("items");
            if (CollectionUtils.isEmpty(items)) {
                return;
            }
            for (ItemEntityVO item : items) {
                if (canBuy(item, sgFrameworkContextContent, contentId)) {
                    canBuyItemList.add(item);
                } else {
                    LogUtil.errorCode(sgFrameworkContextContent.getBizScenario().getUniqueIdentity(),
                            "ITEM_CANBUY_FALSE" + "," + contentId + " " + item.getString("itemId"));
                }
            }

            result.put(contentId, canBuyItemList);
        });

        return result;
    }

    private boolean canBuy(ItemEntityVO item, SgFrameworkContextContent sgFrameworkContextContent, Long contentId) {

        if (itemInfoError(item, sgFrameworkContextContent, contentId)) {
            LogUtil.errorCode(sgFrameworkContextContent.getBizScenario().getUniqueIdentity(),
                    "ITEM_INFO_ERROR" + "," + contentId + " " + item.getString("itemId"));
            return false;
        }

        Boolean canBuy = item.getBoolean("canBuy");
        Boolean sellOut = item.getBoolean("sellOut");

        return (canBuy == null || canBuy) && (sellOut == null || !sellOut);
    }

    private boolean itemInfoError(ItemEntityVO item, SgFrameworkContextContent sgFrameworkContextContent, Long contentId) {
        return StringUtils.isEmpty(item.getString("shortTitle"))
                || StringUtils.isEmpty(item.getString("itemImg"))
                || StringUtils.isEmpty(item.getString("itemMPrice"));
    }
}
