package com.tmall.wireless.tac.biz.processor.icon.item.ext;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.aselfcaptain.item.model.ChannelDataDO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.biz.iteminfo.bysource.ItemInfoSourceKey;
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
import com.tmall.wireless.tac.biz.processor.config.TxcsShoppingguideAppSwitch;
import com.tmall.wireless.tac.client.domain.Context;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C
    , scenario = ScenarioConstantApp.ICON_ITEM
)
public class IconItemItemMetaInfoBuildSdkExtPt extends Register implements ItemMetaInfoBuildSdkExtPt {

    private final static String AB_TEST_RESULT = "abTestVariationsResult";

    public static final String SCENE_CODE = "shoppingguide.category";

    private static final String AB_TEST_CODE = "MAOCHAO_SHOPPINGGUIDE";

    private static final String ICON_AB_TEST_ID = "211";

    @Override
    public ItemMetaInfo process(Context context) {
        ItemMetaInfo itemMetaInfo = new ItemMetaInfo();
        ItemGroupMetaInfo itemGroupMetaInfoSmB2c = ItemGroupMetaInfo.build(BizType.SM.getCode() + "_" + O2oType.B2C.name());
        itemGroupMetaInfoSmB2c.setItemInfoNodes(this.buildDefaultItemInfoNodes(context));
        ItemGroupMetaInfo itemGroupMetaInfoSmOneHour = ItemGroupMetaInfo.build(BizType.SM.getCode() + "_" + O2oType.O2OOneHour.name());
        itemGroupMetaInfoSmOneHour.setItemInfoNodes(this.buildDefaultItemInfoNodes(context));
        ItemGroupMetaInfo itemGroupMetaInfoSmHalfDay = ItemGroupMetaInfo.build(BizType.SM.getCode() + "_" + O2oType.O2OHalfDay.name());
        itemGroupMetaInfoSmHalfDay.setItemInfoNodes(this.buildDefaultItemInfoNodes(context));
        ItemGroupMetaInfo itemGroupMetaInfoSmNextDay = ItemGroupMetaInfo.build(BizType.SM.getCode() + "_" + O2oType.O2ONextDay.name());
        itemGroupMetaInfoSmNextDay.setItemInfoNodes(this.buildDefaultItemInfoNodes(context));
        itemMetaInfo.setItemGroupRenderInfoList(Lists
            .newArrayList(new ItemGroupMetaInfo[] {itemGroupMetaInfoSmB2c, itemGroupMetaInfoSmOneHour, itemGroupMetaInfoSmHalfDay,
                itemGroupMetaInfoSmNextDay}));
        return itemMetaInfo;
    }

    private List<ItemInfoNode> buildDefaultItemInfoNodes(Context context) {
        ItemInfoNode itemInfoNode = new ItemInfoNode();
        ItemInfoSourceMetaInfo item = ItemInfoSourceMetaInfo.build("captain");
        item.setMktSceneCode(SCENE_CODE);
        ItemInfoSourceMetaInfo tppItemInfoSource = ItemInfoSourceMetaInfo.build("tpp");
        ItemInfoSourceMetaInfo smartUiItemInfoSource = ItemInfoSourceMetaInfo.build("smartui");
        smartUiItemInfoSource.setStrategyPackageId("707_11169");

        // 表示走captain管道服务获取数据
        ItemInfoSourceMetaInfo channelDataItemInfoSource = ItemInfoSourceMetaInfo.build("channel");
        // captain入参
        ChannelDataDO mostBuyChannelData = new ChannelDataDO();
        mostBuyChannelData.setChannelField("value");
        // 表示查询的表
        mostBuyChannelData.setChannelName("salePoint");
        // 表示返回字段的field
        mostBuyChannelData.setDataKey(IconBuildItemVoSdkExtPt.MOST_WORTH_BUY_KEY);
        List<ChannelDataDO> datas = Lists.newArrayList(mostBuyChannelData);
        channelDataItemInfoSource.setChannelFields(datas);
        Map<String, String> extraMap = Maps.newHashMap();
        // 约定字段
        extraMap.put("salePointType", "MOST_WORTH_BUYING");
        channelDataItemInfoSource.setExtraMap(extraMap);
        // 降级开关
        if (!TxcsShoppingguideAppSwitch.openMostWorthBuy) {
            channelDataItemInfoSource.setOpenChannelFlag(Boolean.FALSE);
        } else {
            channelDataItemInfoSource.setOpenChannelFlag(getAbData(context));
        }

        List<String> e1 = Lists.newArrayList(new String[] {"supermarketPrice", "timesBot", "salesLast30d"});
        List<String> e2 = Lists.newArrayList(new String[] {"priceLabel", "timesBot", "salesLast30d"});
        List<List<String>> exclusiveMaterials = Lists.newArrayList();
        exclusiveMaterials.add(e1);
        exclusiveMaterials.add(e2);
        smartUiItemInfoSource.setExclusiveMaterials(exclusiveMaterials);
        List<String> requireList = Lists.newArrayList(new String[] {"extVideo5S", "richPict", "whitePict"});
        List<String> requireListPrice = Lists.newArrayList(new String[] {"pagePrice"});
        List<List<String>> requireListList = Lists.newArrayList();
        requireListList.add(requireList);
        requireListList.add(requireListPrice);
        smartUiItemInfoSource.setRequiredMaterials(requireListList);
        smartUiItemInfoSource.setAppId(27642L);

        smartUiItemInfoSource.setMktSceneCode(SCENE_CODE);
        List<ItemInfoSourceMetaInfo> metaInfos = Lists.newArrayList(item, tppItemInfoSource, channelDataItemInfoSource);
        if (TxcsShoppingguideAppSwitch.openSmartUiInIconCategory) {
            metaInfos.add(smartUiItemInfoSource);
        }
        itemInfoNode.setItemInfoSourceMetaInfos(metaInfos);
        return Lists.newArrayList(new ItemInfoNode[] {itemInfoNode});
    }

    private boolean getAbData(Context context) {
        StringBuilder itemSetIdType = new StringBuilder();
        try {
            if (context.getParams().get(AB_TEST_RESULT) == null
                || StringUtils.isBlank(context.getParams().get(AB_TEST_RESULT).toString())) {
                HadesLogUtil.stream(ScenarioConstantApp.ICON_ITEM)
                    .kv("ICON_ITEM context.getParams()", JSON.toJSONString(context.getParams()))
                    .info();
                return Boolean.TRUE;
            }
            List<Map<String, Object>> abTestRest = (List<Map<String, Object>>)context.getParams().get(AB_TEST_RESULT);
            if (CollectionUtils.isEmpty(abTestRest)) {
                HadesLogUtil.stream(ScenarioConstantApp.ICON_ITEM)
                    .kv("SxlItemRecService context.getParams().get(AB_TEST_RESULT)", JSON.toJSONString(context.getParams()))
                    .info();
                return Boolean.TRUE;
            }
            HadesLogUtil.stream(ScenarioConstantApp.ICON_ITEM)
                .kv("SxlItemRecService abTestRest", JSON.toJSONString(abTestRest))
                .info();
            for (Map<String, Object> variation : abTestRest) {
                HadesLogUtil.stream(ScenarioConstantApp.ICON_ITEM)
                    .kv("icon", "getAbData")
                    .kv("iconABType", AB_TEST_CODE)
                    .info();
                if (AB_TEST_CODE.equals(variation.get("bizType")) &&
                      Objects.equals(ICON_AB_TEST_ID, String.valueOf(variation.get("tclsExpId")))) {
                    if (variation.get("mostWorthBuy") != null) {
                        String flag = String.valueOf(variation.get("mostWorthBuy"));
                        return Objects.equals("1", flag);
                    }
                }
            }
        } catch (Exception e) {
            HadesLogUtil.stream(ScenarioConstantApp.ICON_ITEM)
                .kv("ICON_ITEM getAbData", JSON.toJSONString(context.getParams()))
                .kv("e.getMessage()", JSON.toJSONString(e))
                .info();
        }
        HadesLogUtil.stream(ScenarioConstantApp.ICON_ITEM)
            .kv("ICON_ITEM channelData", itemSetIdType.toString())
            .info();
        return Boolean.TRUE;
    }
}
