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
public class TppRequestConfig {

    private Long tppId;

    private String contentType;

    public TppRequestConfig(Long tppId){
        this.tppId=tppId;
    }
}
