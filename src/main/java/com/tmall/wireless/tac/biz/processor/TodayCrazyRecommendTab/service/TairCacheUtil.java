package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ItemFailProcessorRequest;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.model.ErrorCode;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.meta.ItemMetaInfo;
import com.tmall.txcs.gs.model.model.dto.EntityDTO;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.txcs.gs.spi.recommend.TairManager;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.TabTypeEnum;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TairCacheUtil {

    public static final int SAMPLING_INTERVAL = 10;

    private static final AtomicLong counter = new AtomicLong(0L);

    @Autowired
    TairFactorySpi tairFactorySpi;

    @Autowired
    TacLoggerImpl tacLogger;

    String logKey = "originDataFailProcessor";
    String originDataSuccessKey = "originDataSuccess";

    public OriginDataDTO<ItemEntity> process(ItemFailProcessorRequest itemFailProcessorRequest) {

        SgFrameworkContextItem sgFrameworkContextItem = itemFailProcessorRequest.getSgFrameworkContextItem();

        int interval = Optional.of(itemFailProcessorRequest)
                .map(ItemFailProcessorRequest::getSgFrameworkContextItem)
                .map(SgFrameworkContextItem::getItemMetaInfo)
                .map(ItemMetaInfo::getSamplingInterval)
                .orElse(SAMPLING_INTERVAL);
        if (interval <= 0) {
            interval = SAMPLING_INTERVAL;
        }

        String tairKey = buildTairKey(itemFailProcessorRequest);
        long currentCount = counter.addAndGet(1);
        boolean success = checkSuccess(itemFailProcessorRequest.getItemEntityOriginDataDTO());

        TairManager merchantsTair = tairFactorySpi.getOriginDataFailProcessTair();
        if (merchantsTair == null || merchantsTair.getMultiClusterTairManager() == null || merchantsTair.getNameSpace() <= 0) {

            HadesLogUtil.stream(sgFrameworkContextItem.getBizScenario().getUniqueIdentity())
                    .kv("step", logKey)
                    .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_TAIR_MANAGER_NULL)
                    .error();

            return itemFailProcessorRequest.getItemEntityOriginDataDTO();
        }


        if (success) {
            HadesLogUtil.stream(sgFrameworkContextItem.getBizScenario().getUniqueIdentity())
                    .kv("step", logKey)
                    .kv("info", originDataSuccessKey)
                    .info();

            if (currentCount % interval == 1) {
                tacLogger.info("tpp缓存写入了");
                ResultCode put = merchantsTair.getMultiClusterTairManager().put(merchantsTair.getNameSpace(),
                        tairKey,
                        JSON.toJSONString(itemFailProcessorRequest.getItemEntityOriginDataDTO().getResult()),
                        0);

                if (put == null || put.getCode() != ResultCode.SUCCESS.getCode()) {
                    HadesLogUtil.stream(sgFrameworkContextItem.getBizScenario().getUniqueIdentity())
                            .kv("step", logKey)
                            .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_TAIR_PUT_ERROR)
                            .kv("tairKey", tairKey)
                            .error();
                }

            }
            return itemFailProcessorRequest.getItemEntityOriginDataDTO();

        } else {

            HadesLogUtil.stream(sgFrameworkContextItem.getBizScenario().getUniqueIdentity())
                    .kv("step", logKey)
                    .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_ORIGIN_DATA_FAIL)
                    .error();
            tacLogger.info("tpp缓存读取");
            List<ItemEntity> itemEntityList = readFromTair(tairKey, merchantsTair);
            if (org.apache.commons.collections.CollectionUtils.isEmpty(itemEntityList)) {
                HadesLogUtil.stream(sgFrameworkContextItem.getBizScenario().getUniqueIdentity())
                        .kv("step", logKey)
                        .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_READ_FROM_TARI_FAIL)
                        .error();
                return itemFailProcessorRequest.getItemEntityOriginDataDTO();
            }

            HadesLogUtil.stream(sgFrameworkContextItem.getBizScenario().getUniqueIdentity())
                    .kv("step", logKey)
                    .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_READ_FROM_TARI_SUCCESS)
                    .error();

            OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();
            originDataDTO.setResult(itemEntityList);
            originDataDTO.setIndex(0);
            originDataDTO.setHasMore(false);
            originDataDTO.setPvid("");
            originDataDTO.setScm("1007.0.0.0");
            return originDataDTO;
        }

    }

    private String buildTairKey(ItemFailProcessorRequest itemFailProcessorRequest) {
        String tabType = MapUtil.getStringWithDefault(itemFailProcessorRequest.getSgFrameworkContextItem().getRequestParams(), "tabType", TabTypeEnum.TODAY_CHAO_SHENG.getType());
        return "TPP_supermarket.b2c.TODAY_CRAZY_RECOMMEND_TAB_" + tabType;

    }

    private List<ItemEntity> readFromTair(String tairKey, TairManager tairManager) {

        List<ItemEntity> list = Lists.newArrayList();
        Result<DataEntry> dataEntryResult = tairManager.getMultiClusterTairManager().get(tairManager.getNameSpace(), tairKey);

        String value = Optional.ofNullable(dataEntryResult).map(Result::getValue).map(DataEntry::getValue).map(Object::toString).orElse("");

        if (StringUtils.isEmpty(value)) {
            return list;
        }

        return JSON.parseArray(value, ItemEntity.class);
    }


    protected <T extends EntityDTO> boolean checkSuccess(OriginDataDTO<T> originDataDTO) {
        return originDataDTO != null && CollectionUtils.isNotEmpty(originDataDTO.getResult());
    }
}
