package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.item;

import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderCheckUtil;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

public class ItemPriceInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**商品原价*/
    @Setter
    private String reservePrice;

    /**商品优惠价*/
    @Setter
    private String promotionPrice;

    /**商品Detail页面价，如果有商品优惠价则为优惠价，否则为原价*/
    @Setter
    private String detailPagePrice;

    /**商品定向优惠专享价*/
    @Setter
    private String directPromotionPrice;

    /**参考价(过去30天最高到手价)*/
    @Setter
    private String referencePrice;

    /**券后价*/
    @Setter
    private String couponDiscountedPrice;

    /**商品是否优惠价*/
    @Getter@Setter
    private boolean noPromotionPrice=false;

    /**商品是否开启到手价*/
    @Setter @Getter
    private boolean openArrivalPrice = false;

    /**获取商品原价*/
    public String getReservePrice() {
        try{
            if(RenderCheckUtil.StringEmpty(reservePrice)){
                return null;
            }
            return removeLastZero(reservePrice);
        }catch(Exception e){
            return null;
        }
    }

    /**获取商品优惠价*/
    public String getPromotionPrice() {
        try{
            if(RenderCheckUtil.StringEmpty(promotionPrice)){
                return null;
            }
            return removeLastZero(promotionPrice);
        }catch(Exception e){
            return null;
        }
    }

    /**商品Detail页面价，默认就是商品优惠价*/
    public String getDetailPagePrice() {
        try{
            if(RenderCheckUtil.StringEmpty(detailPagePrice)){
                return null;
            }
            return removeLastZero(detailPagePrice);
        }catch(Exception e){
            return null;
        }
    }

    /**商品定向优惠专享价*/
    public String getDirectPromotionPrice() {
        try{
            if(RenderCheckUtil.StringEmpty(directPromotionPrice)){
                return null;
            }
            return removeLastZero(directPromotionPrice);
        }catch(Exception e){
            return null;
        }
    }

    /**获取商品参考价*/
    public String getReferencePrice() {
        try{
            if(RenderCheckUtil.StringEmpty(referencePrice)){
                return null;
            }
            return removeLastZero(referencePrice);
        }catch(Exception e){
            return null;
        }
    }

    /**获取商品券后价*/
    public String getCouponDiscountedPrice() {
        try{
            if(RenderCheckUtil.StringEmpty(couponDiscountedPrice)){
                return null;
            }
            return removeLastZero(couponDiscountedPrice);
        }catch(Exception e){
            return null;
        }
    }

    private boolean twoRetainDot(String smallNumber){
        try{
            if(RenderCheckUtil.StringEmpty(smallNumber)){
                return false;
            }
            String[] smallNumberStr = smallNumber.split("\\.");
            if(smallNumberStr.length != 2){
                return false;
            }
            return smallNumberStr[1].length()==2;
        }catch(Exception e){
            return false;
        }
    }

    /**抹掉价格最后一位0*/
    private String removeLastZero(String price){
        if(price.contains(".") && twoRetainDot(price) && price.endsWith("0")){
            price = price.substring(0,price.length()-1);
        }
        return price;
    }

}
