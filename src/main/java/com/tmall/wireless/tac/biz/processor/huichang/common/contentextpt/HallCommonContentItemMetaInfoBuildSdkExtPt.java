package com.tmall.wireless.tac.biz.processor.huichang.common.contentextpt;

import java.util.List;

import com.alibaba.cola.extension.Extension;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.biz.iteminfo.bysource.ItemInfoSourceKey;
import com.tmall.tcls.gs.sdk.framework.extensions.content.context.ContentItemMetaInfoBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.BizType;
import com.tmall.tcls.gs.sdk.framework.model.context.O2oType;
import com.tmall.tcls.gs.sdk.framework.model.meta.ItemGroupMetaInfo;
import com.tmall.tcls.gs.sdk.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.tcls.gs.sdk.framework.model.meta.ItemMetaInfo;
import com.tmall.tcls.gs.sdk.framework.model.meta.node.ItemInfoNode;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.client.domain.Context;

import static com.tmall.tcls.gs.sdk.biz.extensions.item.contextbuild.DefaultItemMetaInfoBuildSdkExtPt.DEFAULT_SCENE_NAME;

/**
 *
 * @author wangguohui
 */
@Extension(bizId = HallScenarioConstant.HALL_SCENARIO_BIZ_ID,
    useCase = HallScenarioConstant.HALL_SCENARIO_USE_CASE_B2C,
    scenario = HallScenarioConstant.HALL_SCENARIO_SCENARIO)
public class HallCommonContentItemMetaInfoBuildSdkExtPt implements ContentItemMetaInfoBuildSdkExtPt {
    @Override
    public ItemMetaInfo process(Context context) {
        ItemMetaInfo itemMetaInfo = new ItemMetaInfo();
        ItemGroupMetaInfo itemGroupMetaInfoSmB2c = ItemGroupMetaInfo.build(BizType.SM.getCode() + "_" + O2oType.B2C.name());
        itemGroupMetaInfoSmB2c.setItemInfoNodes(buildDefaultItemInfoNodes());


        ItemGroupMetaInfo itemGroupMetaInfoSmOneHour = ItemGroupMetaInfo.build(BizType.SM.getCode() + "_" + O2oType.O2OOneHour.name());
        itemGroupMetaInfoSmOneHour.setItemInfoNodes(buildDefaultItemInfoNodes());

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
        return itemMetaInfo;
    }

    private List<ItemInfoNode> buildDefaultItemInfoNodes() {
        ItemInfoNode itemInfoNode = new ItemInfoNode();
        ItemInfoSourceMetaInfo item= ItemInfoSourceMetaInfo.build(ItemInfoSourceKey.CAPTAIN);
        item.setMktSceneCode(DEFAULT_SCENE_NAME);

        // tpp返回的数据获取埋点、品牌、类目等数据
        ItemInfoSourceMetaInfo tppItemInfoSource = ItemInfoSourceMetaInfo.build(ItemInfoSourceKey.TPP);

        itemInfoNode.setItemInfoSourceMetaInfos(Lists.newArrayList(item, tppItemInfoSource));
        return Lists.newArrayList(itemInfoNode);
    }
}
