package com.tmall.wireless.tac.biz.processor.cnxh;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.aselfcommon.model.todaycrazy.enums.LogicalArea;
import com.tmall.txcs.biz.supermarket.extpt.origindata.ConvertUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.excutor.SgExtensionExecutor;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.support.LogUtil;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.model.model.dto.RecommendResponseEntity;
import com.tmall.txcs.gs.model.model.dto.tpp.RecommendItemEntityDTO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.txcs.gs.spi.recommend.RecommendSpi;
import com.tmall.wireless.tac.biz.processor.cnxh.enums.O2otTypeEnum;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderAddressUtil;
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
    RecommendSpi recommendSpi;
    @Autowired
    private SgExtensionExecutor sgExtensionExecutor;
    @Autowired
    TacLogger tacLogger;

    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem context) {
        RecommendRequest recommendRequest = this.buildTppParams(context);
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

    private RecommendRequest buildTppParams(SgFrameworkContextItem context) {
        String pageId = "pageId";
        String itemBusinessType = "itemBusinessType";
        RecommendRequest recommendRequest = new RecommendRequest();
        Map<String, String> params1 = sgExtensionExecutor.execute(
            ItemOriginDataRequestExtPt.class,
            context.getBizScenario(),
            pt -> pt.process0(context)).getParams();
        Map<String, String> params = new HashMap<>(16);
        String o2oType = MapUtil.getStringWithDefault(context.getRequestParams(), "o2oType", "");
        String csa = MapUtil.getStringWithDefault(context.getRequestParams(), "csa", "");
        Long appId = this.getAppId(o2oType);
        Long index = MapUtil.getLongWithDefault(context.getRequestParams(), "index", 0L);
        Long userId = MapUtil.getLongWithDefault(context.getRequestParams(), "userId", 0L);
        Long pageSize = MapUtil.getLongWithDefault(context.getRequestParams(), "pageSize", 20L);
        Long itemSetId = MapUtil.getLongWithDefault(context.getRequestParams(), "itemSetId", 0L);
        Long smAreaId = context.getLocParams().getSmAreaId() == 0 ? LogicalArea.parseByCode(
            AddressUtil.parseCSA(csa).getRegionCode()).getCoreCityCode() : context.getLocParams().getSmAreaId();
        Long logicAreaId = context.getLocParams().getRegionCode();

        params.put("itemSetIdSource", "crm");
        params.put("pmtSource", "sm_manager");
        params.put("pmtName", "o2oGuessULike");
        params.put("smAreaId", smAreaId + "");
        params.put("userId", String.valueOf(userId));
        params.put("index", index + "");
        params.put("pageSize", pageSize + "");
        params.put(pageId, appId + "");
        params.put("logicAreaId", logicAreaId + "");
        params.put("itemSetIdList", itemSetId + "");
        params.put("isFirstPage", params1.get("isFirstPage"));
        if (O2otTypeEnum.ONE_HOUR.getCode().equals(o2oType)) {
            params.put(pageId, "onehourcnxh");
            params.put(itemBusinessType, "OneHour");
            params.put("rt1HourStoreId", String.valueOf(context.getLocParams().getRt1HourStoreId()));
        } else if (O2otTypeEnum.HALF_DAY.getCode().equals(o2oType)) {
            params.put(pageId, "halfdaycnxh");
            params.put(itemBusinessType, "HalfDay");
            params.put("rtHalfDayStoreId", String.valueOf(context.getLocParams().getRtHalfDayStoreId()));
        } else if (O2otTypeEnum.NEXT_DAY.getCode().equals(o2oType)) {
            params.put(pageId, "nextdaycnxh");
            params.put(itemBusinessType, "NextDay");
        } else if (O2otTypeEnum.ALL_FRESH.getCode().equals(o2oType)) {
            params.put("pageId", "onehourcnxh");
            params.put(itemBusinessType, "B2C");
        }
        recommendRequest.setAppId(appId);
        recommendRequest.setLogResult(true);
        recommendRequest.setParams(params);
        recommendRequest.setUserId(userId);
        return recommendRequest;

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
