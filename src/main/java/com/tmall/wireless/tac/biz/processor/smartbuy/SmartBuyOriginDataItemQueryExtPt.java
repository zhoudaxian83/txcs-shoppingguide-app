package com.tmall.wireless.tac.biz.processor.smartbuy;

import java.util.List;
import java.util.Optional;
import com.alibaba.cola.extension.Extension;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.item.O2oType;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import io.reactivex.Flowable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import static com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.BannerItemInfoOriginDataItemQueryExtPt.defaultBizType;

/**
 * @author guijian
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENE_SMART_BUY_ITEM)
@Service
public class SmartBuyOriginDataItemQueryExtPt implements OriginDataItemQueryExtPt {
    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem sgFrameworkContextItem) {
        OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();
        String itemId = MapUtil.getStringWithDefault(
            Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getRequestParams).orElse(Maps.newHashMap()),
            RequestKeyConstantApp.ITEM_ID,
            "");
        if (StringUtils.isEmpty(itemId)) {
            return Flowable.just(originDataDTO);
        }
        originDataDTO.setResult(buildItemList(itemId));
        return Flowable.just(originDataDTO);
    }
    private List<ItemEntity> buildItemList(String itemId) {
        List<ItemEntity> result = Lists.newArrayList();
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setItemId(Long.valueOf(itemId));
        itemEntity.setO2oType(O2oType.B2C.name());
        itemEntity.setBizType(defaultBizType);
        result.add(itemEntity);
        return result;
    }
}
