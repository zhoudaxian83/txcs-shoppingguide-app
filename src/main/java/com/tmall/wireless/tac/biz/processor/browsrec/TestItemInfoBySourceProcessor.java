package com.tmall.wireless.tac.biz.processor.browsrec;

import com.google.common.collect.Maps;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.txcs.gs.framework.support.itemInfo.ItemInfoRequest;
import com.tmall.txcs.gs.framework.support.itemInfo.bysource.ItemInfoBySourceProcessorI;
import com.tmall.txcs.gs.framework.support.itemInfo.bysource.ItemInfoBySourceResponse;
import com.tmall.txcs.gs.model.item.ItemUniqueId;
import com.tmall.txcs.gs.model.spi.model.ItemInfoBySourceDTO;
import io.reactivex.Flowable;

import java.util.Map;

/**
 * Created by yangqing.byq on 2021/3/20.
 */

public class TestItemInfoBySourceProcessor implements ItemInfoBySourceProcessorI {
    @Override
    public String getItemSetSource() {
        return "test";
    }

    @Override
    public Flowable<ItemInfoBySourceResponse> process(SgFrameworkContextItem sgFrameworkContextItem, ItemInfoRequest itemInfoRequest, ItemInfoSourceMetaInfo itemInfoSourceMetaInfo) {
        Map<ItemUniqueId, ItemInfoBySourceDTO> result = Maps.newHashMap();
        itemInfoRequest.getList().forEach(itemEntity -> {
            ItemInfoBySourceDTO itemInfoBySourceDTO = new ItemInfoBySourceDTO();
            result.put(itemEntity.getItemUniqueId(),itemInfoBySourceDTO );
        });
        return Flowable.just(ItemInfoBySourceResponse.success(result));
    }

    @Override
    public Map<String, Object> convert(ItemInfoBySourceDTO itemInfoBySourceDTO) {
        Map<String, Object> itemInfo = Maps.newHashMap();
        itemInfo.put("test","test");
        return itemInfo;
    }
}
