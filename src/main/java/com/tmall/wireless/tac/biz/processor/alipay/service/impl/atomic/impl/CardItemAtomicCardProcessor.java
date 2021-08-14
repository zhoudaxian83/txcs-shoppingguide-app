package com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.tradecsa.common.service.spi.response.PageFloorAtomicResultDTO;
import com.google.common.collect.Lists;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.AtomicCardProcessRequest;
import com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic.IAtomicCardProcessor;
import org.springframework.stereotype.Service;
import sun.security.krb5.internal.PAData;

// 卡片六宫格商品
@Service
public class CardItemAtomicCardProcessor implements IAtomicCardProcessor {

//    private static String PLACE_HOLDER_IMG_URL =
    private static final String ITEM_TEMPLATE = "{\n" +
        "   \"containerStyle\":{\n" +
        "      \"//\":\"整个grid背景框的底色，依赖服务端下发,写成这样是为了方便写css样式代码\",\n" +
        "      \"backgroundImage\":\"linear-gradient(to bottom,#FFE5D3,#FFDCC4)\"\n" +
        "   },\n" +
        "\t  \"//\":\"是否有头部间隔，因为头部卡片背景里有图片，所以只在头部卡片背景里有图片的case下发为true，默认false\",\n" +
        "    \"hideTopDivider\":\"true\",\n" +
        "   \"items\":[\n" +
        "      {\n" +
        "         \"//\":\"item图片的url\",\n" +
        "         \"image\":\"xxxxxx\",\n" +
        "         \"//\":\"图片点击跳转链接\",\n" +
        "         \"action\":\"{\\\"type\\\":\\\"jump\\\",\\\"link\\\":\\\"alipays://xxx\\\"}\",\n" +
        "         \"//\":\"小item标题\",\n" +
        "         \"title\":\"<span style=\\\\\\\"font-size:26;color:#333333\\\\\\\">红烧牛肉面</span>\",\n" +
        "         \"//\":\"小item标题（两行样式）\",\n" +
        "         \"complexTitle\":\"<span style=\\\\\\\"font-size:26;color:#333333\\\\\\\">红烧牛肉面</span>\",\n" +
        "         \"//\":\"小item副标题\",\n" +
        "         \"subTitle\":\"<span style=\\\\\\\"font-size:24;color:#999999\\\\\\\">淮阳牛肉面</span>\",\n" +
        "         \"//\":\"左上角标签\",\n" +
        "         \"topLabel\":\"<span style=\\\\\\\"font-size:12;color:#999999\\\\\\\">带娃精选</span>\",\n" +
        "         \"topLabelStyle\":{\n" +
        "            \"backgroundColor\":\"#FFFFFF\"\n" +
        "         },\n" +
        "         \"//\":\"口碑特殊供给标签背景图\",\n" +
        "         \"tagImage\":\"imageUrl\",\n" +
        "         \"//\":\"口碑特殊标签左侧文本，要固定大小\",\n" +
        "         \"tagLeftText\":\"<span style=\\\\\\\"font-size:24;color:#FF2F23\\\\\\\">1750起</span>\",\n" +
        "         \"//\":\"口碑特殊标签右侧文本\",\n" +
        "         \"tagRightText\":\"<span style=\\\\\\\"font-size:24;color:#FFFFFF\\\\\\\">抢</span>\",\n" +
        "         \"//\":\"飞猪&商超特殊供给标签背景图\",\n" +
        "         \"tagImageV2\":\"imageUrl\",\n" +
        "         \"//\":\"飞猪&商超特殊供给标签左侧文本，要固定大小\",\n" +
        "         \"tagLeftTextV2\":\"<span style=\\\\\\\"font-size:24;color:#FF2F23\\\\\\\">1750起</span>\",\n" +
        "         \"//\":\"飞猪&商超特殊供给标签右侧文本，要固定大小\",\n" +
        "         \"tagRightTextV2\":\"<span style=\\\\\\\"font-size:24;color:#FF2F23\\\\\\\">1750起</span>\",\n" +
        "         \"//\":\"标签顶部图片\",\n" +
        "         \"tagTopImage\":\"imageUrl\",\n" +
        "         \"//\":\"标签底部文字\",\n" +
        "         \"originalPrice\":\"￥3000\",\n" +
        "         \"originalPriceStyle\":{\n" +
        "            \"color\":\"#FFFFFF\",\n" +
        "            \"fontSize\":\"10sp\",\n" +
        "            \"textDecoration\":\"line-through\"\n" +
        "         },\n" +
        "         \"//\":\"标签'纯文案'文字\",\n" +
        "         \"pureStringText\":\"<span style=\\\\\\\"font-size:12;color:#999999\\\\\\\">领劵更优惠</span>\",\n" +
        "         \"pureStringTextStyle\":{\n" +
        "            \"backgroundColor\":\"#FFFFFF\"\n" +
        "         },\n" +
        "         \"//\":\"单个小item埋点scm\",\n" +
        "         \"scm\":\"xxxx\",\n" +
        "         \"//\":\"单个小item埋点扩展参数\",\n" +
        "         \"remoteLogExt\":\"xxx\",\n" +
        "         \"//\":\"满减价格标签（二房字段控制，下发就展示）\",\n" +
        "         \"saleTags\":[\n" +
        "            {\n" +
        "               \"text\":\"<span style=\\\"font-size:10sp;color:#FF6010\\\">30减2</span>\",\n" +
        "               \"textStyle\":{\n" +
        "                  \"borderColor\":\"#FF6010\"\n" +
        "               }\n" +
        "            },\n" +
        "            {\n" +
        "               \"text\":\"<span style=\\\"font-size:10sp;color:#FF6010\\\">50减6</span>\",\n" +
        "               \"textStyle\":{\n" +
        "                  \"borderColor\":\"#FF6010\"\n" +
        "               }\n" +
        "            }\n" +
        "         ],\n" +
        "         \"//\":\"商品特性标签（二方字段控制，下发就展示）\",\n" +
        "         \"qualityTags\":[\n" +
        "            {\n" +
        "               \"text\":\"<span style=\\\"font-size:10sp;color:#FF6010\\\">养生美食</span>\",\n" +
        "               \"textStyle\":{\n" +
        "                  \"backgroundColor\":\"#FF661A\"\n" +
        "               }\n" +
        "            },\n" +
        "            {\n" +
        "               \"text\":\"<span style=\\\"font-size:10sp;color:#FF6010\\\">汁水很多</span>\",\n" +
        "               \"textStyle\":{\n" +
        "                  \"backgroundColor\":\"#FF661A\"\n" +
        "               }\n" +
        "            }\n" +
        "         ],\n" +
        "        \"//\":\"默认兜底字段，展示优先级（完整版 > 标签兜底 > 文字兜底）最低\",\n" +
        "        \"defaultFooterText\":\"<span style=\\\"font-size:12sp;color:#999999\\\">’30分钟送达\n" +
        "'</span>\"\n" +
        "      }\n" +
        "   ],\n" +
        "   \"spmC\":\"xxx\"\n" +
        "}";

    @Override
    public String atomicCardId() {
        return "CSDTemplate_Topic_Floor_Grid";
    }

    @Override
    public PageFloorAtomicResultDTO process(AtomicCardProcessRequest atomicCardProcessRequest) {
        PageFloorAtomicResultDTO pageFloorAtomicResultDTO = new PageFloorAtomicResultDTO();
        pageFloorAtomicResultDTO.setAtomCardTemplateId(atomicCardProcessRequest.getPageFloorAtomicDTO().getAtomCardTemplateId());
        pageFloorAtomicResultDTO.setCardData(Lists.newArrayList(JSON.parseObject(ITEM_TEMPLATE)));
        return pageFloorAtomicResultDTO;
    }
}
