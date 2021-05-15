package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alibaba.cola.extension.Extension;

import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.extpt.origindata.ConvertUtil;
import com.tmall.txcs.biz.supermarket.extpt.origindata.DefaultOriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.extensions.excutor.SgExtensionExecutor;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.item.O2oType;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.model.model.dto.RecommendResponseEntity;
import com.tmall.txcs.gs.model.model.dto.tpp.RecommendItemEntityDTO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.txcs.gs.spi.recommend.RecommendSpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/15 20:22
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.WU_ZHE_TIAN)
@Service
public class TestWuZheTianOriginDataItemQueryExtPt implements OriginDataItemQueryExtPt {

    Logger LOGGER = LoggerFactory.getLogger(DefaultOriginDataItemQueryExtPt.class);

    @Autowired
    RecommendSpi recommendSpi;
    @Autowired
    private SgExtensionExecutor sgExtensionExecutor;

    private static Map<String, String> tppO2oTypeConvertMap;

    static {
        tppO2oTypeConvertMap = Maps.newHashMap();
        tppO2oTypeConvertMap.putIfAbsent("one_hour", O2oType.O2OOneHour.name());
        tppO2oTypeConvertMap.putIfAbsent("half_day", O2oType.O2OHalfDay.name());
        tppO2oTypeConvertMap.putIfAbsent("next_day", O2oType.O2ONextDay.name());
        tppO2oTypeConvertMap.putIfAbsent("B2C", O2oType.B2C.name());
    }

    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem context) {

        RecommendRequest recommendRequest = sgExtensionExecutor.execute(
            ItemOriginDataRequestExtPt.class,
            context.getBizScenario(),
            pt -> pt.process0(context));

        return recommendSpi.recommendItem(recommendRequest)
            .map(recommendResponseEntityResponse -> {
                // tpp 返回失败
                if (!recommendResponseEntityResponse.isSuccess()
                    || recommendResponseEntityResponse.getValue() == null
                    || CollectionUtils.isEmpty(recommendResponseEntityResponse.getValue().getResult())) {
                    return new OriginDataDTO<>();
                }
                return convert(recommendResponseEntityResponse.getValue());
            });
    }

    private OriginDataDTO<ItemEntity> convert(RecommendResponseEntity<RecommendItemEntityDTO> recommendResponseEntity) {
        OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();

        originDataDTO.setHasMore(recommendResponseEntity.isHasMore());
        originDataDTO.setIndex(recommendResponseEntity.getIndex());
        originDataDTO.setPvid(recommendResponseEntity.getPvid());
        originDataDTO.setScm(recommendResponseEntity.getScm());
        originDataDTO.setTppBuckets(recommendResponseEntity.getTppBuckets());

        originDataDTO.setResult(recommendResponseEntity
            .getResult()
            .stream()
            .filter(Objects::nonNull).map(ConvertUtil::convert).collect(Collectors.toList()));
        return originDataDTO;
    }

}
