package com.tmall.wireless.tac.biz.processor.detail.common.convert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author: guichen
 * @Data: 2021/9/14
 * @Description:
 */
@Component
public class DetailConverterFactory implements InitializingBean {

    public static DetailConverterFactory instance;

    @Resource
    List<AbstractConverter> converterList;

    private static Map<String, AbstractConverter> converters;

    public AbstractConverter getConverter(String type) {
        return converterList.stream()
            .filter(v -> v.isAccess(type))
            .findFirst()
            .orElse(null);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }
}
