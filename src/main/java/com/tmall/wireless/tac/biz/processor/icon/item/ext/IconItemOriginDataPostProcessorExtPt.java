package com.tmall.wireless.tac.biz.processor.icon.item.ext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.google.common.collect.Lists;
import com.tmall.aselfcommon.model.column.MainColumnDTO;
import com.tmall.aselfcommon.model.column.MaterialDTO;
import com.tmall.aselfcommon.model.column.SubColumnDTO;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.origindata.OriginDataProcessRequest;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataPostProcessorExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.item.BizType;
import com.tmall.txcs.gs.model.item.O2oType;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.icon.ColumnCacheService;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRecommendService;
import com.tmall.wireless.tac.biz.processor.icon.item.ItemRequest;
import com.tmall.wireless.tac.biz.processor.icon.model.IconFixedItemDTO;
import com.tmall.wireless.tac.biz.processor.wzt.constant.Constant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * @author zhongwei
 * @date 2021/11/23
 */
@SdkExtension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C
    , scenario = ScenarioConstantApp.ICON_ITEM
)
public class IconItemOriginDataPostProcessorExtPt extends Register implements OriginDataPostProcessorExtPt {

    @Resource
    private ColumnCacheService columnCacheService;

    @Override
    public OriginDataDTO<ItemEntity> process(SgFrameworkContextItem context) {
        OriginDataDTO<ItemEntity> originDataDTO = Optional.of(context).map(SgFrameworkContextItem::getItemEntityOriginDataDTO).orElse(null);
        // 如果是首页，增加定坑
        int index = context.getUserPageInfo().getIndex();
        if (index != 0) {
            return originDataDTO;
        }
        // 较通用定坑场景
        // 1. 所见所得，请求中带的itemId，参考上新了超市做法, icon无；
        Map<String, Object> requestParam = context.getRequestParams();

        // 2. 获取定坑商品数据
        ItemRequest itemRequest = (ItemRequest)Optional.of(context)
            .map(SgFrameworkContextItem::getRequestParams)
            .map(c -> c.get(ItemRecommendService.ITEM_REQUEST_KEY)).orElse(null);
        if (itemRequest == null) {
            return originDataDTO;
        }
        if (!StringUtils.isNumeric(itemRequest.getLevel2Id()) || !StringUtils.isNumeric(itemRequest.getLevel3Id())) {
            return originDataDTO;
        }
        String fixedItemStr = getFixedItemList(Long.parseLong(itemRequest.getLevel2Id()), Long.parseLong((itemRequest.getLevel3Id())));
        if (StringUtils.isBlank(fixedItemStr)) {
            return originDataDTO;
        }
        List<IconFixedItemDTO> fixedItemDTOList = IconFixedItemDTO.getFixedItemByStr(fixedItemStr).stream()
            .filter(v -> v.getBeginTime() != null && v.getEndTime() != null && v.getItemId() != null)
            .filter(v -> v.getEndTime().getTime() > System.currentTimeMillis())
            .filter(v -> v.getBeginTime().getTime() < System.currentTimeMillis())
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(fixedItemDTOList)) {
            return originDataDTO;
        }
        context.getUserParams().put(Constant.FIXED_ITEM, fixedItemDTOList);

        List<Long> fixedItemIds = Lists.newArrayList();
        List<ItemEntity> finalFixedItems = fixedItemDTOList.stream()
            .map(IconFixedItemDTO::getItemId)
            .map(itemId -> {
                fixedItemIds.add(itemId);
                ItemEntity itemEntity = new ItemEntity();
                itemEntity.setItemId(itemId);
                itemEntity.setBizType(BizType.SM.getCode());
                itemEntity.setO2oType(O2oType.B2C.name());
                itemEntity.setBusinessType(O2oType.B2C.name());
                return itemEntity;
            }).collect(Collectors.toList());
        List<ItemEntity> finalItemEntities = Lists.newArrayList();
        finalItemEntities.addAll(finalFixedItems);
        List<ItemEntity> originList = Optional.of(originDataDTO).map(OriginDataDTO::getResult).orElse(Lists.newArrayList());
        originList.forEach(itemEntity -> {
            if (!fixedItemIds.contains(itemEntity.getItemId())) {
                finalItemEntities.add(itemEntity);
            }
        });
        originDataDTO.setResult(finalItemEntities);
        return originDataDTO;
    }

    private String getFixedItemList(Long level2Id, Long level3Id) {

        MainColumnDTO column = columnCacheService.getColumn(level2Id);
        if (column == null) {
            return null;
        }
        SubColumnDTO subColumnDTO = column.getSubColumnDTOMap().get(level3Id);
        if (subColumnDTO == null) {
            return null;
        }
        MaterialDTO materialDTO = subColumnDTO.getMaterialDTOMap().get("fixedItemIdList");
        if (materialDTO == null) {
            return null;
        }
        return materialDTO.getExtValue();

    }
}
