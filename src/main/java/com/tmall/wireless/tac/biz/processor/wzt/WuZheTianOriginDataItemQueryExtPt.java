package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import com.tmall.wireless.tac.biz.processor.wzt.utils.RecommendTairUtil;
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
    }

    @Autowired
    TacLogger tacLogger;

    @Resource
    RecommendTairUtil recommendTairUtil;

    @Autowired
    TairFactorySpi tairFactorySpi;

    private static final int labelSceneNamespace = 184;

    //分大区个性化排序后商品缓存后缀
    private static final String areaSortSuffix = "AREA_SORT";

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
         *
         */

        tacLogger.info("context=" + JSON.toJSONString(context));
        DataContext dataContext = new DataContext();
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
        //params.put("RecItemIds",
        //    "536427844454,582396352306,617524588202,538818102072,586978507246,633753044261,536708195821,582396352306,"
        //        + "617524588202,538818102072,586978507246,633753044261,536708195821,582396352306,617836325106,"
        //        + "540271599415,587516703876,634661347726,536708195821,582396352306");
        //params.put("logicAreaId", "107");
        //params.put("index", "0");
        //params.put("pageSize", "20");
        //params.put("itemLayers", "浅爆,超爆,爆品,浅爆,爆品,浅爆,爆品,超爆,爆品,浅爆,爆品,浅爆,爆品,超爆,爆品,爆品,超爆,爆品,爆品,超爆");
        //params.put("smAreaId", "330100");
        //params.put("relativePrices",
        //    "0.600,0.100,0.100,0.900,0.700,0.100,1.000,0.100,0.100,0.900,0.700,0.100,1.000,0.100,0.700,0.600,0.100,0"
        //        + ".700,1.000,0.100");
        params.put("appid", "21431");
        params.put("userItemIdList",
            "536427844454,582396352306,617524588202,538818102072,586978507246,633753044261,536708195821,582396352306,"
                + "617524588202,538818102072,586978507246,633753044261,536708195821,582396352306,617836325106,"
                + "540271599415,587516703876,634661347726,536708195821,582396352306");
        recommendRequest.setParams(params);
        tacLogger.info("recommendRequest=" + JSON.toJSONString(recommendRequest));
        //获取限购信息
        //ItemLimitResult itemLimitInfoQuery = this.getItemLimitInfo(userId, mockItems);
        //tacLogger.info("itemLimitResult=" + JSON.toJSONString(itemLimitInfoQuery));

        List<Long> areaItems = getItemToCacheOfArea(smAreaId);
        if (areaItems == null) {
            return recommendSpi.recommendItem(recommendRequest)
                .map(recommendResponseEntityResponse -> {
                    tacLogger.info(
                        "recommendResponseEntityResponse=" + JSON.toJSONString(recommendResponseEntityResponse));
                    // tpp 返回失败
                    //if (!recommendResponseEntityResponse.isSuccess()
                    //    || recommendResponseEntityResponse.getValue() == null
                    //    || CollectionUtils.isEmpty(recommendResponseEntityResponse.getValue().getResult())) {
                    //    return new OriginDataDTO<>();
                    //}

                    return this.convert(dataContext);
                });
        } else {
            return Flowable.just(this.convert(dataContext));
        }
    }

    private OriginDataDTO<ItemEntity> convert(DataContext dataContext) {
        List<Long> items = dataContext.getItems();
        List<Long> resultItems = this.getPage(dataContext.getItems(), dataContext.getIndex(),
            dataContext.getPageSize());
        tacLogger.info("items=" + JSON.toJSONString(items));
        OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();
        originDataDTO.setResult(buildItemList(resultItems));
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
        Object o = recommendTairUtil.queryPromotionFromCache(cacheKey);
        if (Objects.isNull(o)) {
            return null;
        }
        return (List<PmtRuleDataItemRuleDTO>)recommendTairUtil.queryPromotionFromCache(cacheKey);
    }

    /**
     * 商品列表入参那构建
     *
     * @return
     */
    private List<ItemEntity> buildItemList(List<Long> items) {
        return items.stream().map(item -> {
            ItemEntity itemEntity = new ItemEntity();
            itemEntity.setItemId(item);
            itemEntity.setO2oType(defaultO2oType);
            itemEntity.setBizType(defaultBizType);
            return itemEntity;
        }).collect(Collectors.toList());
    }

    /**
     * 缓存个性化排序后的商品信息，区分大区
     *
     * @return
     */
    private Boolean setItemToCacheOfArea(List<Long> itemIds, Long smAreaId) {
        LogicalArea logicalArea = LogicalArea.ofCoreCityCode(smAreaId);
        if (logicalArea == null) {
            tacLogger.warn("setItemToCacheOfArea大区id未匹配：smAreaId：" + smAreaId);
            return false;
        }
        return recommendTairUtil.updateItemDetailPromotionCache(itemIds,
            logicalArea.getCoreCityCode() + areaSortSuffix);
    }

    private List<Long> getItemToCacheOfArea(Long smAreaId) {
        LogicalArea logicalArea = LogicalArea.ofCoreCityCode(smAreaId);
        if (logicalArea == null) {
            tacLogger.warn("getItemToCacheOfArea大区id未匹配：smAreaId：" + smAreaId);
            return null;
        }
        Object o = recommendTairUtil.queryPromotionFromCache(logicalArea.getCoreCityCode() + areaSortSuffix);
        return o == null ? null : JSONObject.parseArray(String.valueOf(o), Long.class);
    }

    /**
     * 手动分页
     *
     * @param originList 分页前数据
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @return 分页后结果
     */
    public <T> List<T> getPage(List<T> originList, Long pageNum, Long pageSize) {
        // 如果页码为空或者每页数量为空
        pageNum = pageNum == null ? 0 : pageNum;
        pageSize = pageSize == null ? 0 : pageSize;
        // 分页后的结果
        List<T> resultList = new ArrayList<>();
        // 如果需要进行分页
        if (pageNum > 0 && pageSize > 0) {
            // 获取起点
            long pageStart = (pageNum - 1) * pageSize;
            // 获取终点
            long pageStop = pageStart + pageSize;
            // 开始遍历
            while (pageStart < pageStop) {
                // 考虑到最后一页可能不够pageSize
                if (pageStart == originList.size()) {
                    break;
                }
                resultList.add(originList.get(Math.toIntExact(pageStart++)));
            }
        }
        // 如果不进行分页
        else {
            // 显示所有数据
            resultList = originList;
        }
        return resultList;
    }

}
