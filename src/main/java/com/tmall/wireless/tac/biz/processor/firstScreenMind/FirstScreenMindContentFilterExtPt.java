package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import com.alibaba.cola.extension.Extension;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.gs.framework.extensions.filter.ContentFilterExtPt;
import com.tmall.txcs.gs.framework.extensions.filter.ContentFilterRequest;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.txcs.gs.framework.model.ItemEntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.support.LogUtil;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderContentTypeEnum.recipeContent;

/**
 * Created by yangqing.byq on 2021/7/7.
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
@Service
public class FirstScreenMindContentFilterExtPt implements ContentFilterExtPt {

    Logger LOGGER = LoggerFactory.getLogger(FirstScreenMindContentFilterExtPt.class);

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
                if (skipFilter(contentVO)) {
                    contentListAfterFilter.add(contentVO);
                    return;
                }
                Long contentId = contentVO.getLong("contentId");
                if (contentId2CanBuyItemList.get(contentId) != null
                        && contentId2CanBuyItemList.get(contentId).size() >= CONTENT_ITEM_SIZE) {
                    contentListAfterFilter.add(contentVO);
                    contentVO.put("items", contentId2CanBuyItemList.get(contentId));
                } else {

                    LogUtil.errorCode(contentFilterRequest.getSgFrameworkContextContent().getBizScenario().getUniqueIdentity(),
                            "CONTENT_FILTER_BY_ITEM",
                            contentId + "");
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
            LOGGER.error("FirstScreenMindContentFilterExtPt_error:", e);
        }


        return contentFilterRequest.getContentVOSgFrameworkResponse();
    }

    // 菜谱不做过滤
    private boolean skipFilter(ContentVO contentVO) {
        String contentType = contentVO.getString("contentType");
        return StringUtils.equals(recipeContent.getType(), contentType);
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
                if (canBuy(item)) {
                    canBuyItemList.add(item);
                } else {
                    LogUtil.errorCode(sgFrameworkContextContent.getBizScenario().getUniqueIdentity(),
                            "ITEM_CANBUY_FALSE",
                            contentId + " " + item.getString("641456783229"));
                }
            }

            result.put(contentId, canBuyItemList);
        });

        return result;

    }

    private boolean canBuy(ItemEntityVO item) {
        Boolean canBuy = item.getBoolean("canBuy");
        Boolean sellOut = item.getBoolean("sellOut");

        return (canBuy == null || canBuy) && (sellOut == null || !sellOut);
    }
}
