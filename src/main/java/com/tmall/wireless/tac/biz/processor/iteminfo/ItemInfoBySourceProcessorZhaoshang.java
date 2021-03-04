package com.tmall.wireless.tac.biz.processor.iteminfo;

import com.alibaba.reactive.support.FlowLifts;
import com.google.common.collect.Maps;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.txcs.gs.framework.support.itemInfo.ItemInfoRequest;
import com.tmall.txcs.gs.framework.support.itemInfo.bysource.ItemInfoBySourceDTO;
import com.tmall.txcs.gs.framework.support.itemInfo.bysource.ItemInfoBySourceProcessorI;
import com.tmall.txcs.gs.framework.support.itemInfo.sm.ItemInfoRequestSm;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.item.ItemUniqueId;
import com.tmall.txcs.gs.model.item.O2oType;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.wireless.tac.biz.processor.iteminfo.dto.ItemInfoBySourceDTOZhaoshang;
import io.reactivex.Flowable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by yangqing.byq on 2021/2/26.
 */
@Service
public class ItemInfoBySourceProcessorZhaoshang implements ItemInfoBySourceProcessorI {
    @Override
    public String getItemSetSource() {
        return "招商";
    }


    @Override
    public Flowable<Response<Map<ItemUniqueId, ItemInfoBySourceDTO>>> process(SgFrameworkContextItem sgFrameworkContextItem, ItemInfoRequest itemInfoRequest, ItemInfoSourceMetaInfo itemInfoSourceMetaInfo) {

        return FlowLifts.liftReturnEmptyIfNull(() -> getMap(sgFrameworkContextItem, (ItemInfoRequestSm) itemInfoRequest))
                .map(map -> {
                    Response<Map<ItemUniqueId, ItemInfoBySourceDTO>> response = new Response<>();
                    response.setValue(map);
                    response.setSuccess(true);
                    return response;
                });

    }

    private Map<ItemUniqueId, ItemInfoBySourceDTO> getMap(SgFrameworkContextItem sgFrameworkContextItem, ItemInfoRequestSm itemInfoRequestSm) {
        List<ItemEntity> list = itemInfoRequestSm.getList();
        Map<ItemUniqueId, ItemInfoBySourceDTO> map = Maps.newHashMap();
        String o2oType = itemInfoRequestSm.getO2oType();
        list.forEach(itemEntity -> {
            ItemUniqueId itemUniqueId = ItemUniqueId.ofItemIdAndO2oType(itemEntity.getId(), O2oType.from(o2oType));
            ItemInfoBySourceDTOZhaoshang itemInfoBySourceDTOZhaoshang = new ItemInfoBySourceDTOZhaoshang();
            itemInfoBySourceDTOZhaoshang.setZhaoshangInfo("efdf");
            map.put(itemUniqueId, itemInfoBySourceDTOZhaoshang);
        });
        return map;
    }

    @Override
    public Map<String, Object> convert(ItemInfoBySourceDTO itemInfoBySourceDTO) {
        Map<String, Object> result = Maps.newHashMap();
        ItemInfoBySourceDTOZhaoshang itemInfoBySourceDTOZhaoshang = (ItemInfoBySourceDTOZhaoshang) itemInfoBySourceDTO;
        result.put("res", itemInfoBySourceDTOZhaoshang.getZhaoshangInfo());
        return result;
    }
}
