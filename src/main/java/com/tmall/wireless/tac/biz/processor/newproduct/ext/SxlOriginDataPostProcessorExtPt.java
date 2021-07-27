package com.tmall.wireless.tac.biz.processor.newproduct.ext;

import com.alibaba.cola.extension.Extension;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataPostProcessorExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.model.item.BizType;
import com.tmall.txcs.gs.model.item.O2oType;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.newproduct.constant.Constant;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author haixiao.zhang
 * @date 2021/7/6
 */
@Extension(bizId = ScenarioConstant.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstant.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_SHANG_XIN_ITEM)
@Service
public class SxlOriginDataPostProcessorExtPt implements OriginDataPostProcessorExtPt {

    @Autowired
    TacLogger tacLogger;
    @Override
    public OriginDataDTO<ItemEntity> process(SgFrameworkContextItem sgFrameworkContextItem) {

        OriginDataDTO<ItemEntity> originDataDTO = Optional.of(sgFrameworkContextItem).map(SgFrameworkContextItem::getItemEntityOriginDataDTO).orElse(null);
        String topItemIds = (String)sgFrameworkContextItem.getUserParams().get(Constant.SXL_TOP_ITEM_IDS);

        int index = sgFrameworkContextItem.getUserPageInfo().getIndex();
        tacLogger.info("SxlOriginDataPostProcessorExtPt topItemIds:"+topItemIds);

        tacLogger.info("SxlOriginDataPostProcessorExtPt index:"+index);

        if(StringUtils.isBlank(topItemIds)){
            return originDataDTO;
        }

        List<String> topItemIdListString = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(topItemIds);

        List<Long> topItemIdList = topItemIdListString.stream().filter(StringUtils::isNumeric).map(Long::valueOf).collect(Collectors.toList());

        List<ItemEntity> topItemEntityList = topItemIdList.stream().map(itemId -> {
            ItemEntity itemEntity = new ItemEntity();
            itemEntity.setItemId(itemId);
            itemEntity.setBizType(BizType.SM.getCode());
            itemEntity.setO2oType(O2oType.B2C.name());
            itemEntity.setBusinessType(O2oType.B2C.name());
            return itemEntity;
        }).collect(Collectors.toList());

        List<ItemEntity> finalItemEntities = Lists.newArrayList();

        if (index == 0) {
            finalItemEntities.addAll(topItemEntityList);
        }
        List<ItemEntity> originList = Optional.of(originDataDTO).map(OriginDataDTO::getResult).orElse(Lists.newArrayList());

        originList.forEach(itemEntity -> {
            if (!topItemIdList.contains(itemEntity.getItemId())) {
                finalItemEntities.add(itemEntity);
            }
        });

        originDataDTO.setResult(finalItemEntities);

        return originDataDTO;

    }
}
