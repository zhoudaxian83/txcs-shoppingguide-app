package com.tmall.wireless.tac.biz.processor.iteminfo;

import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.iteminfo.source.captain.ItemInfoBySourceProcessorCaptain;
import com.tmall.txcs.gs.framework.support.itemInfo.bysource.ItemInfoBySourceProcessorFactory;
import com.tmall.txcs.gs.framework.support.itemInfo.bysource.ItemInfoBySourceProcessorI;
import com.tmall.wireless.tac.biz.processor.iteminfo.constant.ItemInfoSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by yangqing.byq on 2021/2/23.
 */
public class ItemInfoBySourceProcessorFactoryImpl implements ItemInfoBySourceProcessorFactory, InitializingBean {

    @Autowired
    ItemInfoBySourceProcessorCaptain itemInfoBySourceProcessorCaptain;

    @Autowired
    ItemInfoBySourceProcessorZhaoshang itemInfoBySourceProcessorZhaoshang;

    private Map<String, ItemInfoBySourceProcessorI> itemInfoBySourceProcessorIMap;

    private void init() {
        itemInfoBySourceProcessorIMap = Maps.newHashMap();
        itemInfoBySourceProcessorIMap.putIfAbsent(itemInfoBySourceProcessorCaptain.getItemSetSource(), itemInfoBySourceProcessorCaptain);
        itemInfoBySourceProcessorIMap.putIfAbsent(itemInfoBySourceProcessorZhaoshang.getItemSetSource(), itemInfoBySourceProcessorZhaoshang);
    }
    @Override
    public ItemInfoBySourceProcessorI get(String s) {
        return itemInfoBySourceProcessorIMap.get(s);
    }

    @Override
    public ItemInfoBySourceProcessorI getDefault() {
        return itemInfoBySourceProcessorCaptain;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }
}
