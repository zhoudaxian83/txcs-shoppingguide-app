package com.tmall.wireless.tac.biz.processor.brandclub.fp;

import com.alibaba.cola.dto.SingleResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.aselfcaptain.item.model.ChannelDataDO;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.render.RenderSpi;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.Op;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.Validate.noNullElements;

@Service
public class BrandContentSetIdService {

    private final static String SCENE_CHANNEL = "sceneLdb";

    private static final String GROUP_MAPPING_PREFIX = "tcls_ugc_scenegroup_mapping_v1";
    @Autowired
    RenderSpi renderSpi;

    public Map<String, Map<String, Object>> getGroupAndBrandMapping(Collection<Long> brandIds) {
        Collection<String> propKeys = Lists.newArrayList("boardSceneGroupIds", "generalSceneGroupIds");
        List<ChannelDataDO> propQuery = propKeys.stream().map(propKey -> {
            ChannelDataDO query = new ChannelDataDO();
            query.setChannelName(SCENE_CHANNEL);
            query.setDataKey(propKey);
            query.setChannelField(propKey);
            return query;
        }).collect(Collectors.toList());
        List<String> keys = brandIds.stream().map(this::genGroupAndBrandMappingKey).collect(Collectors.toList());
        SPIResult<SingleResponse<Map<String, Map<String, Object>>>> singleResponseSPIResult = renderSpi.queryChannelData(propQuery, keys, null);
        return Optional.of(singleResponseSPIResult).map(SPIResult::getData).map(SingleResponse::getData).orElse(Maps.newHashMap());
    }



    private String genGroupAndBrandMappingKey(Long brandId) {
        return genGroupMappingKey("btao", brandId);
    }

    private String genGroupMappingKey(Object... keys) {
        noNullElements(keys);
        return GROUP_MAPPING_PREFIX + "_" + StringUtils.join(keys, "_");
    }
}

