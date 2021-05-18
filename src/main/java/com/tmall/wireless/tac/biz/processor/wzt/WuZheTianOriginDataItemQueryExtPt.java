package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.excutor.SgExtensionExecutor;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.constant.RpmContants;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.model.model.dto.RecommendResponseEntity;
import com.tmall.txcs.gs.model.model.dto.tpp.RecommendItemEntityDTO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.txcs.gs.spi.recommend.RecommendSpi;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.wzt.enums.LogicalArea;
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

    @Autowired
    TacLogger tacLogger;

    @Resource
    RecommendTairUtil recommendTairUtil;

    @Autowired
    TairFactorySpi tairFactorySpi;
    private static final int labelSceneNamespace = 184;

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
        List<Long> mockItems = Lists.newArrayList();
        Long smAreaId = MapUtil.getLongWithDefault(context.getRequestParams(), "smAreaId", 330100L);
        Long userId = MapUtil.getLongWithDefault(context.getRequestParams(), "userId", 0L);
        //tair获取推荐商品
        List<PmtRuleDataItemRuleDTO> pmtRuleDataItemRuleDTOS = this.getTairData(smAreaId);
        tacLogger.info("tair推荐商品=" + JSON.toJSONString(pmtRuleDataItemRuleDTOS));
        //tpp获取个性化排序规则
        RecommendRequest recommendRequest = new RecommendRequest();
        recommendRequest.setAppId(23198L);
        recommendRequest.setLogResult(true);
        Map<String, String> params = Maps.newHashMap();
        params.put("itemIds", "591228976713,615075644541");
        params.put("userId", userId + "");
        params.put("smAreaId", smAreaId + "");
        recommendRequest.setParams(params);
        tacLogger.info("recommendRequest=" + JSON.toJSONString(recommendRequest));

        //获取限购信息
        //ItemLimitResult itemLimitInfoQuery = this.getItemLimitInfo(userId, mockItems);
        //tacLogger.info("itemLimitResult=" + JSON.toJSONString(itemLimitInfoQuery));

        //return Flowable.just(this.convert());
        return recommendSpi.recommendItem(recommendRequest)
            .map(recommendResponseEntityResponse -> {
                // tpp 返回失败
                //if (!recommendResponseEntityResponse.isSuccess()
                //    || recommendResponseEntityResponse.getValue() == null
                //    || CollectionUtils.isEmpty(recommendResponseEntityResponse.getValue().getResult())) {
                //    return new OriginDataDTO<>();
                //}

                return convert(recommendResponseEntityResponse.getValue());
            });
    }

    private OriginDataDTO<ItemEntity> convert(RecommendResponseEntity<RecommendItemEntityDTO> recommendResponseEntity) {
        tacLogger.info("recommendResponseEntity=" + JSON.toJSONString(recommendResponseEntity));
        OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();
        originDataDTO.setResult(buildItemList());
        return originDataDTO;
    }

    //private ItemLimitResult getItemLimitInfo(Long userId, List<Long> itemIds) {
    //    ItemLimitInfoQuery itemLimitInfoQuery = new ItemLimitInfoQuery();
    //    itemLimitInfoQuery.setUserId(0L);
    //    itemLimitInfoQuery.setItemIdList(itemIds);
    //    return todayCrazyLimitFacade.query(itemLimitInfoQuery);
    //}

    private List<PmtRuleDataItemRuleDTO> getTairData(Long smAreaId) {
        LogicalArea logicalArea = LogicalArea.ofCoreCityCode(smAreaId);
        if (logicalArea == null) {
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
    private List<ItemEntity> buildItemList() {
        List<ItemEntity> result = Lists.newArrayList();
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setItemId(591228976713L);
        itemEntity.setO2oType(defaultO2oType);
        itemEntity.setBizType(defaultBizType);
        result.add(itemEntity);

        ItemEntity itemEntity2 = new ItemEntity();
        itemEntity2.setItemId(615075644541L);
        itemEntity2.setO2oType(defaultO2oType);
        itemEntity2.setBizType(defaultBizType);
        result.add(itemEntity2);
        return result;
    }

    /**
     * 缓存个性化排序后的商品信息，区分大区
     *
     * @return
     */
    private Boolean setItemToCacheOfArea() {

        return false;
    }

    private List<Long> getItemToCacheOfArea() {

        return null;
    }

    /**
     * 手动分页
     *
     * @param originList 分页前数据
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @return 分页后结果
     */
    public static <T> List<T> getPage(List<T> originList, Integer pageNum, Integer pageSize) {
        // 如果页码为空或者每页数量为空
        pageNum = pageNum == null ? 0 : pageNum;
        pageSize = pageSize == null ? 0 : pageSize;
        // 分页后的结果
        List<T> resultList = new ArrayList<>();
        // 如果需要进行分页
        if (pageNum > 0 && pageSize > 0) {
            // 获取起点
            int pageStart = (pageNum - 1) * pageSize;
            // 获取终点
            int pageStop = pageStart + pageSize;
            // 开始遍历
            while (pageStart < pageStop) {
                // 考虑到最后一页可能不够pageSize
                if (pageStart == originList.size()) {
                    break;
                }
                resultList.add(originList.get(pageStart++));
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
