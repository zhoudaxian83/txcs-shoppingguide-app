package com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.tmall.aselfmanager.client.columncenter.response.ColumnCenterDataSetItemRuleDTO;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.failprocessor.ItemFailProcessorRequest;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.model.ErrorCode;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.meta.ItemMetaInfo;
import com.tmall.txcs.gs.model.constant.RpmContants;
import com.tmall.txcs.gs.model.model.dto.EntityDTO;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.txcs.gs.spi.recommend.TairManager;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.CommonConstant;
import com.tmall.wireless.tac.biz.processor.TodayCrazyRecommendTab.constant.TabTypeEnum;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class TodayCrazyTairCacheService {
    private static final String[] logicalArr = {"HD", "HB", "HN", "HZ", "XN"};

    public static final int SAMPLING_INTERVAL = 10;

    private static final AtomicLong counter = new AtomicLong(0L);

    private static final BizScenario bizScenario = BizScenario.valueOf(
            ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
            ScenarioConstantApp.LOC_TYPE_B2C,
            ScenarioConstantApp.TODAY_CRAZY_RECOMMEND_TAB
    );

    @Autowired
    TairFactorySpi tairFactorySpi;

    @Autowired
    TacLoggerImpl tacLogger;

    String logKey = "originDataFailProcessor";
    String originDataSuccessKey = "originDataSuccess";

    public OriginDataDTO<ItemEntity> process(ItemFailProcessorRequest itemFailProcessorRequest) {
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

            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                    .kv("step", logKey)
                    .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_TAIR_MANAGER_NULL)
                    .error();

            return itemFailProcessorRequest.getItemEntityOriginDataDTO();
        }


        if (success) {
            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                    .kv("step", logKey)
                    .kv("info", originDataSuccessKey)
                    .info();

            if (currentCount % interval == 1) {
                tacLogger.info("tpp缓存写入,key：" + tairKey);
                ResultCode put = merchantsTair.getMultiClusterTairManager().put(merchantsTair.getNameSpace(),
                        tairKey,
                        JSON.toJSONString(itemFailProcessorRequest.getItemEntityOriginDataDTO().getResult()),
                        0);

                if (put == null || put.getCode() != ResultCode.SUCCESS.getCode()) {
                    tacLogger.info("tpp缓存写入失败");
                    HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                            .kv("step", logKey)
                            .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_TAIR_PUT_ERROR)
                            .kv("tairKey", tairKey)
                            .error();
                }

            }
            return itemFailProcessorRequest.getItemEntityOriginDataDTO();

        } else {

            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                    .kv("step", logKey)
                    .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_ORIGIN_DATA_FAIL)
                    .error();
            tacLogger.info("tpp缓存读取,key：" + tairKey);
            List<ItemEntity> itemEntityList = readFromTair(tairKey, merchantsTair);
            if (CollectionUtils.isEmpty(itemEntityList)) {
                tacLogger.info("tpp缓存读取失败");
                HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                        .kv("step", logKey)
                        .kv("errorCode", ErrorCode.ITEM_FAIL_PROCESSOR_READ_FROM_TARI_FAIL)
                        .error();
                return itemFailProcessorRequest.getItemEntityOriginDataDTO();
            }
            tacLogger.info("tpp缓存读取成功");
            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
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

    /**
     * 区分线上线下
     *
     * @param itemFailProcessorRequest
     * @return
     */
    private String buildTairKey(ItemFailProcessorRequest itemFailProcessorRequest) {
        String tabType = MapUtil.getStringWithDefault(itemFailProcessorRequest.getSgFrameworkContextItem().getRequestParams(), "tabType", TabTypeEnum.TODAY_CHAO_SHENG.getType());
        if (RpmContants.enviroment.isOnline()) {
            return "TPP_supermarket_b2c_TODAY_CRAZY_RECOMMEND_TAB_" + tabType;
        } else {
            return "TPP_supermarket_b2c_TODAY_CRAZY_RECOMMEND_TAB_" + tabType + "_pre";
        }
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


    /**
     * @param originDataDTO
     * @param <T>
     * @return
     */
    protected <T extends EntityDTO> boolean checkSuccess(OriginDataDTO<T> originDataDTO) {
        return originDataDTO != null && CollectionUtils.isNotEmpty(originDataDTO.getResult());
    }

    public List<ColumnCenterDataSetItemRuleDTO> getTairManager(String tairKey) {
        TairManager tairManager = tairFactorySpi.getOriginDataFailProcessTair();
        if (tairManager == null || tairManager.getMultiClusterTairManager() == null || tairManager.getNameSpace() <= 0) {
            return null;
        }
        Result<DataEntry> dataEntryResult = tairManager.getMultiClusterTairManager().get(tairManager.getNameSpace(), tairKey);
        if (!dataEntryResult.isSuccess() || dataEntryResult.getValue() == null || dataEntryResult.getValue().getValue() == null) {
            return null;
        }
        JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(dataEntryResult.getValue().getValue()));
        if (jsonArray.size() == 0) {
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(jsonArray.get(0)));
        if (jsonObject == null) {
            return null;
        }
        Object dataSetItemRuleDTOList = jsonObject.get("dataSetItemRuleDTOList");
        if (dataSetItemRuleDTOList == null) {
            return null;
        }
        return JSON.parseArray(JSONObject.toJSONString(dataSetItemRuleDTOList), ColumnCenterDataSetItemRuleDTO.class);

    }


    /**
     * 渠道立减商品
     */
    public List<ColumnCenterDataSetItemRuleDTO> getEntryChannelPriceNew() {
        String channelPriceKey = getChannelPriceNewKey();
        return this.getTairManager(channelPriceKey);
    }

    /**
     * 单品折扣价商品
     */
    public List<ColumnCenterDataSetItemRuleDTO> getEntryPromotionPrice() {
        String promotionPriceKey = getPromotionPriceKey();
        return this.getTairManager(promotionPriceKey);
    }


    private String getRandomLogical() {
        try {
            int index = new Random().nextInt(logicalArr.length);
            return logicalArr[index];
        } catch (Exception e) {
        }
        return logicalArr[0];
    }

    private String getChannelPriceNewKey() {//channelPrice_XN_pre
        return this.createKey(CommonConstant.channelPriceNewPrefix, getRandomLogical());
    }

    private String getPromotionPriceKey() {//channelPrice_XN_pre
        return this.createKey(CommonConstant.promotionPricePrefix, getRandomLogical());
    }

    public String createKey(String prefix, Object... args) {
        try {
            checkArgument(args.length > 0, "key args.length can not be 0");
            StringBuilder buf = new StringBuilder(prefix);
            for (Object arg : args) {
                buf.append(String.valueOf(checkNotNull(arg, "key arg"))).append('_');
            }
            // 用于预发环境测试(新的key)
            if (RpmContants.enviroment.isPreline()) {
                buf.append("pre_");
            }
            return buf.substring(0, buf.length() - 1);
        } catch (Exception e) {
            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                    .kv("step", logKey)
                    .kv("errorCode", "GoCreateKeyExc")
                    .error();
        }
        return "";
    }
}
