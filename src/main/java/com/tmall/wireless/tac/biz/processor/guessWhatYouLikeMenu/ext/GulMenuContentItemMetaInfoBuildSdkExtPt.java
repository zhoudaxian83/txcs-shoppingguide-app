package com.tmall.wireless.tac.biz.processor.guessWhatYouLikeMenu.ext;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.biz.iteminfo.bysource.ItemInfoSourceKey;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.context.ContentItemMetaInfoBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.BizType;
import com.tmall.tcls.gs.sdk.framework.model.context.O2oType;
import com.tmall.tcls.gs.sdk.framework.model.meta.ItemGroupMetaInfo;
import com.tmall.tcls.gs.sdk.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.tcls.gs.sdk.framework.model.meta.ItemMetaInfo;
import com.tmall.tcls.gs.sdk.framework.model.meta.node.ItemInfoNode;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.guessWhatYouLikeMenu.constant.ConstantValue;
import com.tmall.wireless.tac.client.domain.Context;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Yushan
 * @date 2021/9/8 8:25 下午
 */
@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.B2C_CNXH_MENU_FEEDS
)
@Service
public class GulMenuContentItemMetaInfoBuildSdkExtPt extends Register implements ContentItemMetaInfoBuildSdkExtPt {
    @Override
    public ItemMetaInfo process(Context context) {

        ItemMetaInfo itemMetaInfo = new ItemMetaInfo();
        /*
        ItemGroupMetaInfo itemGroupMetaInfoSmB2c = ItemGroupMetaInfo.build(BizType.SM.getCode() + "_" + O2oType.B2C.name());
        itemGroupMetaInfoSmB2c.setItemInfoNodes(buildDefaultItemInfoNodes());
         */

        ItemGroupMetaInfo itemGroupMetaInfoSmOneHour = ItemGroupMetaInfo.build(BizType.SM.getCode() + "_" + O2oType.O2OOneHour.name());
        itemGroupMetaInfoSmOneHour.setItemInfoNodes(buildDefaultItemInfoNodes());

        /*
        ItemGroupMetaInfo itemGroupMetaInfoSmHalfDay = ItemGroupMetaInfo.build(BizType.SM.getCode() + "_" + O2oType.O2OHalfDay.name());
        itemGroupMetaInfoSmHalfDay.setItemInfoNodes(buildDefaultItemInfoNodes());

        ItemGroupMetaInfo itemGroupMetaInfoSmNextDay = ItemGroupMetaInfo.build(BizType.SM.getCode() + "_" + O2oType.O2ONextDay.name());
        itemGroupMetaInfoSmNextDay.setItemInfoNodes(buildDefaultItemInfoNodes());

        itemMetaInfo.setItemGroupRenderInfoList(Lists.newArrayList(
                itemGroupMetaInfoSmB2c
                ,itemGroupMetaInfoSmOneHour
                ,itemGroupMetaInfoSmHalfDay
                ,itemGroupMetaInfoSmNextDay
        ));
         */
        itemMetaInfo.setItemGroupRenderInfoList(Lists.newArrayList(itemGroupMetaInfoSmOneHour));
        return itemMetaInfo;
    }

    private List<ItemInfoNode> buildDefaultItemInfoNodes() {
        ItemInfoNode itemInfoNode = new ItemInfoNode();
        ItemInfoSourceMetaInfo item= ItemInfoSourceMetaInfo.build(ItemInfoSourceKey.CAPTAIN);
        item.setMktSceneCode(ConstantValue.SCENE_CODE);

        // tpp返回的数据获取埋点、品牌、类目等数据
        ItemInfoSourceMetaInfo tppItemInfoSource = ItemInfoSourceMetaInfo.build(ItemInfoSourceKey.TPP);

        itemInfoNode.setItemInfoSourceMetaInfos(Lists.newArrayList(item, tppItemInfoSource));
        return Lists.newArrayList(itemInfoNode);
    }
}
