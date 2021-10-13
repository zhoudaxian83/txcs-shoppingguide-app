package com.tmall.wireless.tac.biz.processor.icon.item.ext;

import java.util.List;

import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextbuild.ItemMetaInfoBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.BizType;
import com.tmall.tcls.gs.sdk.framework.model.context.O2oType;
import com.tmall.tcls.gs.sdk.framework.model.meta.ItemGroupMetaInfo;
import com.tmall.tcls.gs.sdk.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.tcls.gs.sdk.framework.model.meta.ItemMetaInfo;
import com.tmall.tcls.gs.sdk.framework.model.meta.node.ItemInfoNode;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.domain.Context;

@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C
    , scenario = ScenarioConstantApp.ICON_ITEM
)
public class IconItemItemMetaInfoBuildSdkExtPt extends Register implements ItemMetaInfoBuildSdkExtPt {

    @Override
    public ItemMetaInfo process(Context context) {
        ItemMetaInfo itemMetaInfo = new ItemMetaInfo();
        ItemGroupMetaInfo itemGroupMetaInfoSmB2c = ItemGroupMetaInfo.build(BizType.SM.getCode() + "_" + O2oType.B2C.name());
        itemGroupMetaInfoSmB2c.setItemInfoNodes(this.buildDefaultItemInfoNodes());
        ItemGroupMetaInfo itemGroupMetaInfoSmOneHour = ItemGroupMetaInfo.build(BizType.SM.getCode() + "_" + O2oType.O2OOneHour.name());
        itemGroupMetaInfoSmOneHour.setItemInfoNodes(this.buildDefaultItemInfoNodes());
        ItemGroupMetaInfo itemGroupMetaInfoSmHalfDay = ItemGroupMetaInfo.build(BizType.SM.getCode() + "_" + O2oType.O2OHalfDay.name());
        itemGroupMetaInfoSmHalfDay.setItemInfoNodes(this.buildDefaultItemInfoNodes());
        ItemGroupMetaInfo itemGroupMetaInfoSmNextDay = ItemGroupMetaInfo.build(BizType.SM.getCode() + "_" + O2oType.O2ONextDay.name());
        itemGroupMetaInfoSmNextDay.setItemInfoNodes(this.buildDefaultItemInfoNodes());
        itemMetaInfo.setItemGroupRenderInfoList(Lists
            .newArrayList(new ItemGroupMetaInfo[]{itemGroupMetaInfoSmB2c, itemGroupMetaInfoSmOneHour, itemGroupMetaInfoSmHalfDay, itemGroupMetaInfoSmNextDay}));
        return itemMetaInfo;
    }

    private List<ItemInfoNode> buildDefaultItemInfoNodes() {
        ItemInfoNode itemInfoNode = new ItemInfoNode();
        ItemInfoSourceMetaInfo item = ItemInfoSourceMetaInfo.build("captain");
        item.setMktSceneCode("index.guessULike");
        ItemInfoSourceMetaInfo tppItemInfoSource = ItemInfoSourceMetaInfo.build("tpp");
        ItemInfoSourceMetaInfo smartUiItemInfoSource = ItemInfoSourceMetaInfo.build("smartui");
        smartUiItemInfoSource.setStrategyPackageId("707_11169");
        List<String> e1 = Lists.newArrayList(new String[]{"supermarketPrice", "timesBot", "salesLast30d"});
        List<String> e2 = Lists.newArrayList(new String[]{"priceLabel", "timesBot", "salesLast30d"});
        List<List<String>> exclusiveMaterials = Lists.newArrayList();
        exclusiveMaterials.add(e1);
        exclusiveMaterials.add(e2);
        smartUiItemInfoSource.setExclusiveMaterials(exclusiveMaterials);
        List<String> requireList = Lists.newArrayList(new String[]{"extVideo5S", "richPict", "whitePict"});
        List<String> requireListPrice = Lists.newArrayList(new String[]{"pagePrice"});
        List<List<String>> requireListList = Lists.newArrayList();
        requireListList.add(requireList);
        requireListList.add(requireListPrice);
        smartUiItemInfoSource.setRequiredMaterials(requireListList);
        smartUiItemInfoSource.setAppId(27642L);
        smartUiItemInfoSource.setMktSceneCode("index.guessULike");
        itemInfoNode.setItemInfoSourceMetaInfos(Lists.newArrayList(new ItemInfoSourceMetaInfo[]{item,
            tppItemInfoSource,smartUiItemInfoSource}));
//        HadesLogUtil.stream(ScenarioConstantApp.ICON_ITEM)
//            .kv("IconItemItemMetaInfoBuildSdkExtPt","buildDefaultItemInfoNodes")
//            .kv("itemInfoNode", JSON.toJSONString(itemInfoNode))
//            .info();
        return Lists.newArrayList(new ItemInfoNode[]{itemInfoNode});
    }
}
