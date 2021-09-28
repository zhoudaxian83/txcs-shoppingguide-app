package com.tmall.wireless.tac.biz.processor.detail.model.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: guichen
 * @Data: 2021/9/16
 * @Description:
 */
@Data
@AllArgsConstructor
public class DetailRequestConfig {

    private Long tppId;

    private SizeDTO sizeDTO;

    private boolean openBackUp=false;

    private int backUpWriteRate = 0;

    public DetailRequestConfig(Long tppId,SizeDTO sizeDTO){
        this.tppId=tppId;
        this.sizeDTO=sizeDTO;
    }
}
