package com.tmall.wireless.tac.biz.processor.firstScreenMind.model.item;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class ItemIndividualInfo implements Serializable {

    private static final long serialVersionUID = -1L;

    /**商品置顶*/
    @Getter @Setter
    private int stick;

    /**商品scm埋点*/
    @Getter @Setter
    private String scm;

}
