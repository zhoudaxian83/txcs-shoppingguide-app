package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alibaba.cola.extension.Extension;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model.BannerItemDTO;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.uitl.BannerUtil;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by yangqing.byq on 2021/4/6.
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.WU_ZHE_TIAN)
public class WuZheTianItemInfoOriginDataItemQueryExtPt implements OriginDataItemQueryExtPt {

    public static final String defaultBizType = "sm";
    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem sgFrameworkContextItem) {

        OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();


        String bannerInfo = MapUtil.getStringWithDefault(
                Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getRequestParams).orElse(Maps.newHashMap()),
                RequestKeyConstantApp.BANNER_INFO,
                "");

        if (StringUtils.isEmpty(bannerInfo)) {
            return Flowable.just(originDataDTO);
        }

        originDataDTO.setResult(buildItemList(bannerInfo));
        return Flowable.just(originDataDTO);
    }

    private List<ItemEntity> buildItemList(String bannerInfo) {
        List<ItemEntity> result = Lists.newArrayList();
        Map<String, List<BannerItemDTO>> bannerIndex2ItemList = BannerUtil.parseBannerItem(bannerInfo);

        if (MapUtils.isEmpty(bannerIndex2ItemList)) {
            return result;
        }

        bannerIndex2ItemList.keySet().forEach(key -> {
            List<BannerItemDTO> bannerItemDTOList = bannerIndex2ItemList.get(key);
            if (CollectionUtils.isEmpty(bannerItemDTOList)) {
                return;
            }
            List<ItemEntity> itemEntityList = bannerItemDTOList.stream().map(bannerItemDTO -> {
                ItemEntity itemEntity = new ItemEntity();
                itemEntity.setItemId(bannerItemDTO.getItemId());
                itemEntity.setO2oType(bannerItemDTO.getLocType());
                itemEntity.setBizType(defaultBizType);
                return itemEntity;
            }).collect(Collectors.toList());
            result.addAll(itemEntityList);
        });

        return result;
    }
}
