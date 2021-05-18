package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import com.ali.com.google.common.base.Joiner;
import com.ali.unit.rule.util.lang.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.extpt.origindata.ConvertUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.excutor.SgExtensionExecutor;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.constant.RpmContants;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.model.model.dto.RecommendResponseEntity;
import com.tmall.txcs.gs.model.model.dto.tpp.RecommendItemEntityDTO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.txcs.gs.spi.recommend.RecommendSpi;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.wzt.enums.LogicalArea;
import com.tmall.wireless.tac.biz.processor.wzt.model.DataContext;
import com.tmall.wireless.tac.biz.processor.wzt.model.PmtRuleDataItemRuleDTO;
import com.tmall.wireless.tac.biz.processor.wzt.utils.TairUtil;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author luojunchong
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.WU_ZHE_TIAN)
@Service
public class WuZheTianOriginDataItemQueryExtPt implements OriginDataItemQueryExtPt {

    private static List<Long> mockItems = Lists.newArrayList();

    static {
        mockItems.add(591228976713L);
        mockItems.add(615075644541L);
        mockItems.add(536427844454L);
        mockItems.add(538818102072L);
        mockItems.add(617524588202L);
        mockItems.add(586978507246L);
        mockItems.add(536708195821L);
        mockItems.add(634661347726L);
        mockItems.add(587516703876L);
        mockItems.add(633753044261L);
        mockItems.add(617836325106L);

    }

    @Autowired
    TacLogger tacLogger;

    @Resource
    TairUtil tairUtil;

    @Autowired
    TairFactorySpi tairFactorySpi;

    private static final int labelSceneNamespace = 184;

    //分大区个性化排序后商品缓存后缀
    private static final String AREA_SORT_SUFFIX = "AREA_SORT";

    public static final String defaultBizType = "sm";
    public static final String defaultO2oType = "B2C";

    //@HSFConsumer(serviceVersion = "1.0.0")
    //private TodayCrazyLimitFacade todayCrazyLimitFacade;

    @Autowired
    RecommendSpi recommendSpi;

    @Autowired
    private SgExtensionExecutor sgExtensionExecutor;

    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem context) {
        /**
         * 1、tair获取商品列表
         * 2、tpp渲染个性化排序
         * 3、排序商品存入tair供下次使用
         * 4、获取前20作为当前页数据
         * 5、查询限购信息
         * 6、captain获取商品数据
         * 7、处理过滤逻辑
         * 8、转换为vo给前端展示
         */

        tacLogger.info("context=" + JSON.toJSONString(context));
        DataContext dataContext = new DataContext();
        dataContext.setItems(mockItems);
        Long smAreaId = MapUtil.getLongWithDefault(context.getRequestParams(), "smAreaId", 330100L);
        Long userId = MapUtil.getLongWithDefault(context.getRequestParams(), "userId", 0L);
        Long index = MapUtil.getLongWithDefault(context.getRequestParams(), "index", 0L);
        Long pageSize = MapUtil.getLongWithDefault(context.getRequestParams(), "pageSize", 20L);
        dataContext.setIndex(index);
        dataContext.setPageSize(pageSize);
        //tair获取推荐商品
        List<PmtRuleDataItemRuleDTO> pmtRuleDataItemRuleDTOS = this.getTairItems(smAreaId);
        tacLogger.info("tair推荐商品=" + JSON.toJSONString(pmtRuleDataItemRuleDTOS));

        //tpp获取个性化排序规则
        RecommendRequest recommendRequest = new RecommendRequest();
        Map<String, String> params = Maps.newHashMap();
        recommendRequest.setLogResult(true);
        recommendRequest.setUserId(userId);
        recommendRequest.setAppId(21431L);
        params.put("userItemIdList", Joiner.on(",").join(mockItems));
        recommendRequest.setParams(params);
        tacLogger.info("recommendRequest=" + JSON.toJSONString(recommendRequest));
        //获取限购信息
        //ItemLimitResult itemLimitInfoQuery = this.getItemLimitInfo(userId, mockItems);
        //tacLogger.info("itemLimitResult=" + JSON.toJSONString(itemLimitInfoQuery));

        OriginDataDTO<ItemEntity> cacheOriginDataDTO = getItemToCacheOfArea(smAreaId);
        if (cacheOriginDataDTO == null) {
            return recommendSpi.recommendItem(recommendRequest)
                .map(recommendResponseEntityResponse -> {
                    tacLogger.info(
                        "recommendResponseEntityResponse.getValue()=" + JSON
                            .toJSONString(recommendResponseEntityResponse.getValue().getResult()));
                    if (!recommendResponseEntityResponse.isSuccess()
                        || recommendResponseEntityResponse.getValue() == null
                        || CollectionUtils.isEmpty(recommendResponseEntityResponse.getValue().getResult())) {
                        return new OriginDataDTO<>();
                    }
                    //需要做缓存
                    OriginDataDTO<ItemEntity> originDataDTO = convert(recommendResponseEntityResponse.getValue());
                    this.setItemToCacheOfArea(originDataDTO, smAreaId);
                    tacLogger.info("缓存生效-非缓存中的数据" + JSON.toJSONString(originDataDTO));
                    return this.getItemPage(originDataDTO, dataContext);
                });
        } else {
            tacLogger.info("缓存生效-是缓存中的数据" + JSON.toJSONString(cacheOriginDataDTO));
            return Flowable.just(this.getItemPage(cacheOriginDataDTO, dataContext));
        }
    }

    private OriginDataDTO<ItemEntity> getItemPage(OriginDataDTO<ItemEntity> originDataDTO, DataContext dataContext) {
        List<ItemEntity> itemEntities = this.getPage(originDataDTO.getResult(), dataContext.getIndex(),
            dataContext.getPageSize());
        tacLogger.info("分页后数据" + JSON.toJSONString(itemEntities));
        originDataDTO.setResult(itemEntities);
        return originDataDTO;
    }

    /**
     * 未缓存前
     *
     * @param recommendResponseEntity
     * @return
     */
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
     * 转换成需要的数据格式，
     *
     * @param list
     * @return
     */
    private List<Long> itemsDataConvert(List<PmtRuleDataItemRuleDTO> list) {
        return mockItems;
    }

    //private ItemLimitResult getItemLimitInfo(Long userId, List<Long> itemIds) {
    //    ItemLimitInfoQuery itemLimitInfoQuery = new ItemLimitInfoQuery();
    //    itemLimitInfoQuery.setUserId(0L);
    //    itemLimitInfoQuery.setItemIdList(itemIds);
    //    return todayCrazyLimitFacade.query(itemLimitInfoQuery);
    //}

    private List<PmtRuleDataItemRuleDTO> getTairItems(Long smAreaId) {
        LogicalArea logicalArea = LogicalArea.ofCoreCityCode(smAreaId);
        if (logicalArea == null) {
            tacLogger.warn("getTairData大区id未匹配：smAreaId：" + smAreaId);
            return null;
        }
        String cacheKey = logicalArea.getCacheKey();
        if (RpmContants.enviroment.isPreline()) {
            cacheKey = cacheKey + "_pre";
        }
        tacLogger.warn("当前环境校验：" + RpmContants.enviroment.isPreline() + "|" + RpmContants.enviroment.isDaily() + "|"
            + RpmContants.enviroment.isOnline());
        Object o = tairUtil.queryPromotionFromCache(cacheKey);
        if (Objects.isNull(o)) {
            return null;
        }
        return (List<PmtRuleDataItemRuleDTO>)tairUtil.queryPromotionFromCache(cacheKey);
    }

    /**
     * 缓存个性化排序后的商品信息，区分大区
     *
     * @return
     */
    private Boolean setItemToCacheOfArea(OriginDataDTO<ItemEntity> originDataDTO, Long smAreaId) {
        LogicalArea logicalArea = LogicalArea.ofCoreCityCode(smAreaId);
        if (logicalArea == null) {
            tacLogger.warn("setItemToCacheOfArea大区id未匹配：smAreaId：" + smAreaId);
            return false;
        }
        return tairUtil.updateItemDetailPromotionCache(originDataDTO,
            logicalArea.getCacheKey() + AREA_SORT_SUFFIX);
    }

    private OriginDataDTO<ItemEntity> getItemToCacheOfArea(Long smAreaId) {
        LogicalArea logicalArea = LogicalArea.ofCoreCityCode(smAreaId);
        if (logicalArea == null) {
            tacLogger.warn("getItemToCacheOfArea大区id未匹配：smAreaId：" + smAreaId);
            return null;
        }
        Object o = tairUtil.queryPromotionFromCache(logicalArea.getCacheKey() + AREA_SORT_SUFFIX);
        if (o == null) {
            return null;
        }
        return JSON.parseObject((String)o, new TypeReference<OriginDataDTO<ItemEntity>>() {});
    }

    /**
     * 手动分页
     *
     * @param originalList 分页前数据
     * @param index        页码
     * @param pageSize     每页数量
     * @return 分页后结果
     */
    public <T> List<T> getPage(List<T> originalList, Long index, Long pageSize) {
        if (index < 1) {
            index = 1L;
        }
        if (index * pageSize > originalList.size()) {
            return Lists.newArrayList();
        }
        tacLogger.info("分页信息" + index + pageSize);
        // 分页后的结果
        List<T> resultList = new ArrayList<>();
        // 如果需要进行分页
        if (pageSize > 0) {
            // 获取起点
            long pageStart = (index - 1) * pageSize;
            // 获取终点
            long pageStop = pageStart + pageSize;
            // 开始遍历
            while (pageStart < pageStop) {
                // 考虑到最后一页可能不够pageSize
                if (pageStart == originalList.size()) {
                    break;
                }
                resultList.add(originalList.get(Math.toIntExact(pageStart++)));
            }
        }
        // 如果不进行分页
        else {
            // 显示所有数据
            resultList = originalList;
        }
        return resultList;
    }

}
