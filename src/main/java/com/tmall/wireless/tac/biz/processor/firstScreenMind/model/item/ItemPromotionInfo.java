package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.item;

import com.tcls.mkt.atmosphere.model.response.ItemPromotionResp;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ItemPromotionInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**优先级最高店铺优惠(满减满折)*/
    @Getter @Setter
    private String shopPromotion;

    /**店铺优惠列表*/
    @Getter @Setter
    private List<String> shopPromotionList;

    /**利益点列表*/
    @Getter @Setter
    private List<String> itemBenefitList;

    /**优惠名称，如聚划算、淘抢购*/
    @Getter @Setter
    private String promotionName;

    /**是否包邮*/
    @Getter @Setter
    private boolean freeShipping;

    /**优惠活动ID*/
    @Getter @Setter
    private Long promActivityId;

    /**优惠活动详情ID*/
    @Getter @Setter
    private Long promActivityDetailId;

    /**优惠开始时间*/
    @Getter @Setter
    private Date promotionStartTime;

    /**优惠结束时间*/
    @Getter @Setter
    private Date promotionEndTime;

    /**限购总量*/
    @Getter @Setter
    private Integer promTotalLimit;

    /**限购总量使用*/
    @Getter @Setter
    private Integer promTotalUsed;

    /**单用户限购总量*/
    @Getter @Setter
    private Integer promPerLimit;

    /**单用户限购使用量*/
    @Getter @Setter
    private Integer promPerUsed;

    /**优惠有效状态*/
    @Getter @Setter
    private boolean promValidStatus;

    /**优惠过滤原因*/
    @Getter @Setter
    private String promFilterReason;

    @Getter @Setter
    private ItemPromotionResp itemPromotionResp;

}
