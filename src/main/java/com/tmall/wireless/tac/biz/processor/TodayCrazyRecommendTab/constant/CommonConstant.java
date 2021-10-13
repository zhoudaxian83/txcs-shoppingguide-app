package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant;

public class CommonConstant {
    /**
     * true打开限购开关,如果不需要限购逻辑关闭即可
     */
    public static final boolean LIMIT_BUY_SWITCH = true;
    public static final String SUPER_MARKET_TODAY_CRAZY = "superMarket_todayCrazy";

    public static final String TODAY_CHANNEL_NEW = "today_channel_new";
    public static final String TODAY_PROMOTION = "today_promotion";
    public static final String ITEM_ID_AND_CACHE_KEYS = "ItemIdAndCacheKeys";

    public static final Long MAX_INDEX = 10000L;

    /**
     * 来自算法
     */
    public static final String TODAY_ALGORITHM = "today_algorithm";

    /**
     * 专享价,渠道价
     */
    public static final String TODAY_CHANNEL_NEW_FEATURED = "today_channel_new_featured";
    /**
     * 通透价
     */
    public static final String TODAY_PROMOTION_FEATURED = "today_promotion_featured";

    /**
     * ald资源位信息
     */
    public static final String ITEM_ALD_RES_ID = "7337863";

    /**
     * 今日疯抢渠道专享价缓存前缀
     */
    public static final String channelPriceNewPrefix = "channelPriceNew_";

    /**
     * 今日疯抢单品优惠价(通投价)缓存前缀
     */
    public static final String promotionPricePrefix = "promotionPrice_";
}
