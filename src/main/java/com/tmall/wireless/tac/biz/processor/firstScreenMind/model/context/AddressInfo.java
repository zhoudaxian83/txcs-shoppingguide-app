package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Joiner;
import com.taobao.util.CollectionUtil;
import lombok.Getter;
import lombok.Setter;

public class AddressInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**四级区域地址ID*/
    @Getter @Setter
    private Long smAreaId;

    /**三级城市ID*/
    @Getter @Setter
    private Long cityCode;

    /**五大区域ID列表*/
    @Getter @Setter
    private List<String> regionIdList = new ArrayList<>();

    /**当前页头的csa地址信息*/
    @Getter @Setter
    private String csa;

    /**门店类型:一小时达、半日达、外仓*/
    @Getter @Setter
    private String storeType;

    /**一小时达门店ID*/
    @Getter @Setter
    private Long rt1HourStoreId;

    /**半日达门店ID*/
    @Getter @Setter
    private Long rtHalfDayStoreId;

    /**外仓门店ID*/
    @Getter @Setter
    private Long rtNextDayStoreId;

    public String getRegionIdStr(){
        if(CollectionUtil.isEmpty(regionIdList)){
            return "";
        }
        return Joiner.on(",").join(regionIdList);
    }

    /**当前页头地址是否被门店覆盖*/
    public boolean rtStoreCover(){
        return rt1HourStoreCover() || rtHalfDayStoreCover();
    }

    public boolean rt1HourStoreCover(){
        return rt1HourStoreId != null && rt1HourStoreId > 0;
    }

    public boolean rtHalfDayStoreCover(){
        return rtHalfDayStoreId != null && rtHalfDayStoreId > 0;
    }
}
