package com.tmall.wireless.tac.biz.processor.iteminfo;

import com.tmall.txcs.gs.framework.support.itemInfo.bysource.ItemInfoBySourceProcessorFactory;
import com.tmall.txcs.gs.framework.support.itemInfo.bysource.ItemInfoBySourceProcessorI;
import org.springframework.stereotype.Service;

/**
 * Created by yangqing.byq on 2021/2/23.
 */
@Service
public class ItemInfoBySourceProcessorFactoryImpl implements ItemInfoBySourceProcessorFactory {

    @Override
    public ItemInfoBySourceProcessorI get(String s) {
        return null;
    }

    @Override
    public ItemInfoBySourceProcessorI getDefault() {
        return null;
    }
}
