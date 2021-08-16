package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class ExposureInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**商品曝光过滤ID列表*/
    @Getter @Setter
    private List<String> itemExposureIdList = new ArrayList<>();

    /**内容曝光过滤ID列表*/
    @Getter @Setter
    private List<String> contentExposureIdList = new ArrayList<>();

}
