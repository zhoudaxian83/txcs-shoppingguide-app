package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.item;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

public class ItemInventoryInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**商品库存数量*/
    @Getter @Setter
    private Integer quantity;
    /**商品月销量标签 5.1万*/
    @Setter
    private String monthlySaleLabel;
    /**商品月销量 53456*/
    @Setter @Getter
    private Integer monthlySaleNumber;

    /*** 是否有库存，true-已售罄*/
    public boolean isSellout() {
        return quantity != null && quantity <= 0;
    }

    /**格式化获取商品月销量*/
    public String getMonthlySaleLabel(){
        try{
            if(monthlySaleNumber<10000){
                return String.valueOf(monthlySaleNumber);
            }
            double afterBought = (double) monthlySaleNumber;
            //1.将数字转换成以万为单位的数字
            double num = afterBought / 10000;
            BigDecimal b = new BigDecimal(num);
            //2.转换后的数字四舍五入保留小数点后一位;
            double finalBought = b.setScale(1,BigDecimal.ROUND_DOWN).doubleValue();
            return finalBought+"万";
        }catch (Exception e){
            return String.valueOf(monthlySaleNumber);
        }
    }

}
