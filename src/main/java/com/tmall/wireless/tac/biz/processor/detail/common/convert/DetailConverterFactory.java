package com.tmall.wireless.tac.biz.processor.detail.common.convert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;

/**
 * @author: guichen
 * @Data: 2021/9/14
 * @Description:
 */
public class DetailConverterFactory implements InitializingBean {

    public static DetailConverterFactory instance;

    @Resource
    List<AbstractConverter> converterList;

    private static Map<String, AbstractConverter> converters;

    public AbstractConverter getConverter(String type){
        return converters.get(type);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
        converters=new HashMap<>(converterList.size());
        converterList.forEach(abstractConverter -> {
            converters.put(abstractConverter.getRecTypeEnum().getType(),abstractConverter);
        });
    }
}
