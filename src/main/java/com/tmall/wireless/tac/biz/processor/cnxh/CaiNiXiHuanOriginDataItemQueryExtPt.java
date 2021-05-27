package com.tmall.wireless.tac.biz.processor.cnxh;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.extpt.origindata.ConvertUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.excutor.SgExtensionExecutor;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.meta.ItemMetaInfo;
import com.tmall.txcs.gs.framework.model.meta.ItemRecommendMetaInfo;
import com.tmall.txcs.gs.framework.support.LogUtil;
import com.tmall.txcs.gs.model.item.O2oType;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.model.model.dto.RecommendResponseEntity;
import com.tmall.txcs.gs.model.model.dto.tpp.RecommendItemEntityDTO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.txcs.gs.spi.recommend.RecommendSpi;
import com.tmall.txcs.gs.spi.recommend.RecommendSpiV2;
import com.tmall.wireless.tac.biz.processor.cnxh.enums.O2otTypeEnum;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author luojunchong
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.O2O_CNXH)
@Service
public class CaiNiXiHuanOriginDataItemQueryExtPt implements OriginDataItemQueryExtPt {

    Logger LOGGER = LoggerFactory.getLogger(CaiNiXiHuanOriginDataItemQueryExtPt.class);

    @Autowired
    TacLogger tacLogger;
    @Autowired
    RecommendSpi recommendSpi;
    @Autowired
    RecommendSpiV2 recommendSpiV2;
    @Autowired
    private SgExtensionExecutor sgExtensionExecutor;

    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem context) {
        String o2oType = MapUtil.getStringWithDefault(context.getRequestParams(), "o2oType", "");
        //两种分页模式，index优先page
        Long index = MapUtil.getLongWithDefault(context.getRequestParams(), "index", 0L);
        Long appId = this.getAppId(o2oType);
        RecommendRequest recommendRequest = sgExtensionExecutor.execute(
            ItemOriginDataRequestExtPt.class,
            context.getBizScenario(),
            pt -> pt.process0(context));
        recommendRequest.setAppId(appId);
        recommendRequest.getParams().put("index", index + "");
        tacLogger.info("tpp入参：" + JSON.toJSONString(recommendRequest));
        long startTime = System.currentTimeMillis();
        return (recommendSpi.recommendItem(recommendRequest))
            .map(recommendResponseEntityResponse -> {
                // tpp 返回失败
                if (!recommendResponseEntityResponse.isSuccess()
                    || recommendResponseEntityResponse.getValue() == null
                    || CollectionUtils.isEmpty(recommendResponseEntityResponse.getValue().getResult())) {
                    LogUtil.info(context.getBizScenario().getUniqueIdentity(),
                        "recommendSpi",
                        "recommendSpi",
                        false,
                        JSON.toJSONString(recommendResponseEntityResponse),
                        System.currentTimeMillis() - startTime
                    );
                    return new OriginDataDTO<>();
                }
                LogUtil.info(context.getBizScenario().getUniqueIdentity(),
                    "recommendSpi",
                    "recommendSpi",
                    true,
                    getTppLogInfo(recommendResponseEntityResponse.getValue()),
                    System.currentTimeMillis() - startTime
                );
                return convert(recommendResponseEntityResponse.getValue());
            });
    }

    private String getTppLogInfo(RecommendResponseEntity<RecommendItemEntityDTO> recommendResponseEntityResponse) {
        StringBuilder log = new StringBuilder("hasMore:" + recommendResponseEntityResponse.isHasMore() + "|");
        recommendResponseEntityResponse.getResult().forEach(
            recommendItemEntityDTO -> {
                log.append(recommendItemEntityDTO.getItemId()).append(",");
                log.append(recommendItemEntityDTO.getO2oType()).append("|");
            }
        );
        return log.toString();
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

    /**
     * 默认21896L
     *
     * @param code
     * @return
     */
    private Long getAppId(String code) {
        O2otTypeEnum o2otTypeEnum = O2otTypeEnum.ofCode(code);
        if (o2otTypeEnum != null) {
            o2otTypeEnum.getAppId();
        }
        return O2otTypeEnum.ALL_FRESH.getAppId();

    }

}
