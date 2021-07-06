package com.tmall.wireless.tac.biz.processor.newproduct.ext;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataPostProcessorExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.model.item.ItemUniqueId;
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

        if(StringUtils.isBlank(topItemIds) || index == 0){
            return originDataDTO;
        }else{
            ItemEntity itemEntity = new ItemEntity();
            itemEntity.setBizType("sm");
            itemEntity.setO2oType("B2C");
            itemEntity.setBusinessType("B2C");
            originDataDTO.getResult().add(0,itemEntity);
        }

        List<ItemEntity> finalItemEntities = Lists.newArrayList();
        Set<String> itemUniqueKeySet = Sets.newHashSet();
        originDataDTO.getResult().forEach(itemEntity -> {
            ItemUniqueId itemUniqueId = itemEntity.getItemUniqueId();
            if (itemUniqueKeySet.contains(itemUniqueId.toString())) {
                return;
            }
            itemUniqueKeySet.add(itemEntity.getItemUniqueId().toString());
            finalItemEntities.add(itemEntity);

        });
        originDataDTO.setResult(finalItemEntities);
        tacLogger.info("SxlOriginDataPostProcessorExtPt originDataDTO:"+ JSON.toJSONString(finalItemEntities));

        return originDataDTO;

    }
}
