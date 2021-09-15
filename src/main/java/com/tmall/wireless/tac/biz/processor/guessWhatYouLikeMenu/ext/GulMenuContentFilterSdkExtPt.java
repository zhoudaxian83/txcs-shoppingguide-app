package com.tmall.wireless.tac.biz.processor.guessWhatYouLikeMenu.ext;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.vo.ContentFilterSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.txcs.gs.framework.support.LogUtil;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.guessWhatYouLikeMenu.constant.ConstantValue;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Yushan
 * @date 2021/9/10 12:54 上午
 */
@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.CNXH_MENU_FEEDS
)
public class GulMenuContentFilterSdkExtPt extends Register implements ContentFilterSdkExtPt {

    @Resource
    TacLogger logger;

    @Override
    public SgFrameworkResponse<ContentVO> process(SgFrameworkContextContent sgFrameworkContextContent) {
        try {
            SgFrameworkResponse<ContentVO> sgFrameworkResponse = sgFrameworkContextContent.getContentVOSgFrameworkResponse();
            List<ContentVO> itemAndContentList = sgFrameworkResponse.getItemAndContentList();
            if (CollectionUtils.isEmpty(itemAndContentList)) {
                return sgFrameworkResponse;
            }
            List<ContentVO> contentListAfterFilter = Lists.newArrayList();
            itemAndContentList.forEach(contentVO -> {
                Long contentId = contentVO.getLong("contentId");
                List<ItemEntityVO> canBuyItemList = Lists.newArrayList();
                List<ItemEntityVO> items = (List<ItemEntityVO>)contentVO.get("items");
                if (CollectionUtils.isEmpty(items)) {
                    return;
                }
                // 库存过滤
                for (ItemEntityVO item : items) {
                    if (canBuy(item, sgFrameworkContextContent, contentId)) {
                        canBuyItemList.add(item);
                    } else {
                        HadesLogUtil.stream(ScenarioConstantApp.CNXH_MENU_FEEDS)
                                .kv("GulMenuContentFilterSdkExtPt","executeFlowable")
                                .kv("ITEM_CANBUY_FALSE", "contentId: " + contentId + " itemId: " + item.getString("itemId"))
                                .error();
                    }
                }
                // 主料筛查 --> 菜谱过滤
                List<ItemEntityVO> mainMaterials = Lists.newArrayList();
                for (ItemEntityVO itemCanBuy : canBuyItemList) {
                    logger.info("****ItemEntityVO:****" + JSON.toJSONString(itemCanBuy));
                    String crowdIds = itemCanBuy.getString("crowdId");
                    logger.info("========crowIds:=======" + crowdIds);
                    // 主料数>=2 跳出
                    if (mainMaterials.size() >= ConstantValue.MAIN_MATERIAL_NUMBER) {
                        break;
                    }
                    // 商品属于不同主料
                    if (!CollectionUtils.isEmpty(mainMaterials) && !mainMaterials.get(0).getString("crowdId").equals(crowdIds)) {
                        mainMaterials.add(itemCanBuy);
                    }
                    // 无主料
                    if (CollectionUtils.isEmpty(mainMaterials)) {
                        mainMaterials.add(itemCanBuy);
                    }
                }
                if (!CollectionUtils.isEmpty(mainMaterials) && mainMaterials.size() == ConstantValue.MAIN_MATERIAL_NUMBER) {
                    contentVO.put("items", mainMaterials);
                    contentListAfterFilter.add(contentVO);
                } else {
                    HadesLogUtil.stream(ScenarioConstantApp.CNXH_MENU_FEEDS)
                            .kv("GulMenuContentFilterSdkExtPt","executeFlowable")
                            .kv("MAIN_MATERIAL_INSUFFICIENT", "contentId: " + contentId)
                            .error();
                }
            });
            sgFrameworkResponse.setItemAndContentList(contentListAfterFilter);
            return sgFrameworkResponse;
        } catch (Exception e) {
            logger.error("GulMenuContentFilterSdkExtPt_error", e);
        }
        return sgFrameworkContextContent.getContentVOSgFrameworkResponse();
    }

    private boolean canBuy(ItemEntityVO item, SgFrameworkContextContent sgFrameworkContextContent, Long contentId) {

        if (itemInfoError(item)) {
            LogUtil.errorCode(sgFrameworkContextContent.getBizScenario().getUniqueIdentity(),
                    "ITEM_INFO_ERROR" + "," + contentId + " " + item.getString("itemId"));
            return false;
        }

        Boolean canBuy = item.getBoolean("canBuy");
        Boolean sellOut = item.getBoolean("sellOut");

        return (canBuy == null || canBuy) && (sellOut == null || !sellOut);
    }

    private boolean itemInfoError(ItemEntityVO item) {
        return StringUtils.isEmpty(item.getString("shortTitle"))
                || StringUtils.isEmpty(item.getString("itemImg"));
    }
}
