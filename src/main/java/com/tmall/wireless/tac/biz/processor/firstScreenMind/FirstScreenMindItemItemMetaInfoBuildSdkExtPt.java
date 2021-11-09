package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import java.util.List;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.tmall.aselfcaptain.item.model.ChannelDataDO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.biz.iteminfo.bysource.ItemInfoSourceKey;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextbuild.ItemMetaInfoBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.BizType;
import com.tmall.tcls.gs.sdk.framework.model.context.O2oType;
import com.tmall.tcls.gs.sdk.framework.model.meta.ItemGroupMetaInfo;
import com.tmall.tcls.gs.sdk.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.tcls.gs.sdk.framework.model.meta.ItemMetaInfo;
import com.tmall.tcls.gs.sdk.framework.model.meta.node.ItemInfoNode;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.config.TxcsShoppingguideAppSwitch;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderContentTypeEnum;
import com.tmall.wireless.tac.client.domain.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_ITEM)
public class FirstScreenMindItemItemMetaInfoBuildSdkExtPt extends Register implements ItemMetaInfoBuildSdkExtPt {


    Logger LOGGER = LoggerFactory.getLogger(FirstScreenMindItemItemMetaInfoBuildSdkExtPt.class);

    public static final String DEFAULT_SCENE_NAME = "default_scene";

    private static final String CHANNEL_NAME = "sceneLdb";

    @Override
    public ItemMetaInfo process(Context context) {
        LOGGER.error("FirstScreenMindItemItemMetaInfoBuildSdkExtPt.context:{}", JSON.toJSONString(context));
        //HadesLogUtil.stream(ScenarioConstantApp.SCE
        HadesLogUtil.stream("HadesLogUtil.FirstScreenMindItemItemMetaInfoBuildSdkExtPt.context")
            .kv("context",JSON.toJSONString(context))
            .error();
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
        ItemInfoSourceMetaInfo item= ItemInfoSourceMetaInfo.build(ItemInfoSourceKey.CAPTAIN);
        item.setMktSceneCode(DEFAULT_SCENE_NAME);

        // tpp返回的数据获取埋点、品牌、类目等数据
        ItemInfoSourceMetaInfo tppItemInfoSource = ItemInfoSourceMetaInfo.build(ItemInfoSourceKey.TPP);


        //isBangdan(context);
        //新增榜单信息查询
        ItemInfoSourceMetaInfo channelDataItemInfoSource = ItemInfoSourceMetaInfo.build("channel");
        ChannelDataDO mostBuyChannelData = new ChannelDataDO();
        mostBuyChannelData.setChannelField("data");
        mostBuyChannelData.setChannelName(CHANNEL_NAME);
        mostBuyChannelData.setDataKey("data");
        List<ChannelDataDO> datas = Lists.newArrayList(mostBuyChannelData);
        //channelDataItemInfoSource.setChannelFields(datas);
        //Map<String, String> extraMap = Maps.newHashMap();
        //extraMap.put("salePointType", "MOST_WORTH_BUYING");
        //channelDataItemInfoSource.setExtraMap(extraMap);



        itemInfoNode.setItemInfoSourceMetaInfos(Lists.newArrayList(item, tppItemInfoSource));

        return Lists.newArrayList(new ItemInfoNode[]{itemInfoNode});
    }

    private boolean isBangdan(SgFrameworkContext sgFrameworkContext) {
        String contentType = MapUtil.getStringWithDefault(sgFrameworkContext.getRequestParams(), RequestKeyConstantApp.CONTENT_TYPE, RenderContentTypeEnum.b2cNormalContent.getType());
        return RenderContentTypeEnum.bangdanContent.getType().equals(contentType)
            || RenderContentTypeEnum.bangdanO2OContent.getType().equals(contentType);
    }

}
