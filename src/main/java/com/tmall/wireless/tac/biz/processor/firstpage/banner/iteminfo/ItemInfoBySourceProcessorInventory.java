package com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo;

import com.google.common.collect.Maps;
import com.tmall.aselfcaptain.item.constant.Channel;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.aselfcaptain.item.model.ItemId;
import com.tmall.aselfcaptain.item.model.ItemQueryDO;
import com.tmall.aselfcaptain.item.model.QueryOptionDO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.iteminfo.source.captain.ItemInfoBySourceDTOMain;
import com.tmall.txcs.gs.framework.model.ErrorCode;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.meta.ItemInfoSourceMetaInfo;
import com.tmall.txcs.gs.framework.support.itemInfo.ItemInfoRequest;
import com.tmall.txcs.gs.framework.support.itemInfo.bysource.ItemInfoBySourceProcessorI;
import com.tmall.txcs.gs.framework.support.itemInfo.bysource.ItemInfoBySourceResponse;
import com.tmall.txcs.gs.framework.support.itemInfo.sm.ItemInfoRequestSm;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.constant.RpmContants;
import com.tmall.txcs.gs.model.item.ItemUniqueId;
import com.tmall.txcs.gs.model.spi.model.ItemDataDTO;
import com.tmall.txcs.gs.model.spi.model.ItemDataRequest;
import com.tmall.txcs.gs.model.spi.model.ItemInfoBySourceDTO;
import com.tmall.txcs.gs.service.facade.CaptainFacade;
import com.tmall.txcs.gs.service.model.CaptainRequest;
import com.tmall.txcs.gs.spi.recommend.ItemDataSpi;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model.ItemInfoBySourceDTOInv;
import io.reactivex.Flowable;
import lombok.Data;
import org.apache.commons.collections.MapUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by yangqing.byq on 2021/5/1.
 */
@Deprecated
public class ItemInfoBySourceProcessorInventory implements ItemInfoBySourceProcessorI {

    @Resource
    private CaptainFacade captainFacade;

    @Override
    public String getItemSetSource() {
        return "inventory";
    }

    @Override
    public Flowable<ItemInfoBySourceResponse> process(SgFrameworkContextItem contextItem, ItemInfoRequest itemInfoRequest, ItemInfoSourceMetaInfo itemInfoSourceMetaInfo) {
        ItemInfoRequestSm itemInfoRequestSm = (ItemInfoRequestSm) itemInfoRequest;


        ItemDataRequest itemDataRequest = new ItemDataRequest();
        itemDataRequest.setList(itemInfoRequest.getList());
        itemDataRequest.setO2oType(itemInfoRequestSm.getO2oType());
        itemDataRequest.setUserId(Optional.ofNullable(contextItem).map(SgFrameworkContext::getUserDO).map(UserDO::getUserId).orElse(0L));
        itemDataRequest.setStoreId(itemInfoRequestSm.getStoreId());
        itemDataRequest.setSmAreaId(Optional.ofNullable(contextItem).map(SgFrameworkContext::getLocParams).map(LocParams::getSmAreaId).orElse(0L));

        CaptainRequest captainRequest = getCaptainRequest(itemDataRequest);



        return captainFacade.call(captainRequest)
                .map(captainResponse -> {
                    Map<ItemUniqueId, ItemInfoBySourceDTO> resultMap = Maps.newHashMap();
                    if (!captainResponse.isPresent() || MapUtils.isEmpty(captainResponse.get())) {
                        HadesLogUtil.logSPIServerCode("aselfcaptain", "error");


                        itemInfoRequest.getList().forEach(itemEntity -> {
                            ItemUniqueId itemUniqueId = itemEntity.getItemUniqueId();
                            ItemInfoBySourceDTOInv itemInfoBySourceDTOInv = new ItemInfoBySourceDTOInv();
                            itemInfoBySourceDTOInv.setCanBuy(true);
                            resultMap.put(itemUniqueId, itemInfoBySourceDTOInv);
                        });
                        return ItemInfoBySourceResponse.success(resultMap);
                    }


                    HadesLogUtil.logSPIServerCode("aselfcaptain", "success");
                    Map<ItemUniqueId, ItemDTO> itemUniqueIdItemDTOMap = captainResponse.get();

                    itemInfoRequest.getList().forEach(itemEntity -> {
                        ItemUniqueId itemUniqueId = itemEntity.getItemUniqueId();

                        ItemDTO itemDTO = itemUniqueIdItemDTOMap.get(itemUniqueId);

                        ItemInfoBySourceDTOInv itemInfoBySourceDTOInv = new ItemInfoBySourceDTOInv();
                        if (itemDTO != null) {
                            itemInfoBySourceDTOInv.setCanBuy(itemDTO.isCanBuy() || !itemDTO.isSoldout());
                        } else {
                            itemInfoBySourceDTOInv.setCanBuy(true);
                        }
                        resultMap.put(itemUniqueId, itemInfoBySourceDTOInv);
                    });
                    return ItemInfoBySourceResponse.success(resultMap);

                });


    }

    @Override
    public Map<String, Object> convert(ItemInfoBySourceDTO itemInfoBySourceDTO) {
        Map<String, Object> result = Maps.newHashMap();
        result.put("1", "1");
        return result;
    }



    private CaptainRequest getCaptainRequest(ItemDataRequest itemDataRequest) {
        CaptainRequest captainRequest = new CaptainRequest();

        ItemQueryDO query = new ItemQueryDO();

        List<ItemId> itemIdList = itemDataRequest.getList().stream().map(itemEntity -> ItemId.valueOf(itemEntity.getItemId(), ItemId.ItemType.valueOf(itemEntity.getO2oType()))).collect(Collectors.toList());
        query.setItemIds(itemIdList);
        query.setBuyerId(itemDataRequest.getUserId());
        query.setSource(RpmContants.APP_NAME);
        query.setChannel(Channel.WAP);
        query.setLocationId(itemDataRequest.getStoreId());
        query.setAreaId(itemDataRequest.getSmAreaId());



        QueryOptionDO option = new QueryOptionDO();
        option.setIncludeQuantity(true);
        option.setIncludeSales(true);
        option.setIncludeItemTags(true);
        option.setIncludeItemFeature(true);
        option.setIncludeMaiFanCard(true);

        //fixme
        option.setSceneCode("index.guessULike");
        option.setOpenMkt(true);

        captainRequest.setQuery(query);
        captainRequest.setOption(option);
        return captainRequest;
    }
}
