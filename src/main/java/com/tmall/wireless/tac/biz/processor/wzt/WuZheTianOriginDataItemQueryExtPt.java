package com.tmall.wireless.tac.biz.processor.wzt;

import com.ali.com.google.common.base.Joiner;
import com.ali.unit.rule.util.lang.CollectionUtils;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

import com.google.common.collect.Maps;
import com.tmall.aself.shoppingguide.client.loc.domain.AddressDTO;
import com.tmall.txcs.biz.supermarket.extpt.origindata.ConvertUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.model.model.dto.RecommendResponseEntity;
import com.tmall.txcs.gs.model.model.dto.tpp.RecommendItemEntityDTO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.txcs.gs.spi.recommend.RecommendSpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.wzt.enums.LogicalArea;
import com.tmall.wireless.tac.biz.processor.wzt.model.ColumnCenterDataSetItemRuleDTO;
import com.tmall.wireless.tac.biz.processor.wzt.model.DataContext;
import com.tmall.wireless.tac.biz.processor.wzt.utils.LogicPageUtil;
import com.tmall.wireless.tac.biz.processor.wzt.utils.SmAreaIdUtil;
import com.tmall.wireless.tac.biz.processor.wzt.utils.TairUtil;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Autowired
    TairUtil tairUtil;

    @Autowired
    RecommendSpi recommendSpi;

    /**
     * 分大区个性化排序后商品缓存后缀
     */
    private static final String AREA_SORT_SUFFIX = "_AREA_SORT";

    private static final String LOG_PREFIX = "WuZheTianOriginDataItemQueryExtPt-";

    private static final Long APP_ID = 21431L;

    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem context) {
        DataContext dataContext = new DataContext();
        String csa = MapUtil.getStringWithDefault(context.getRequestParams(), "csa",
            "13278278282_0_38.066124.114.465406_0_0_0_130105_107_0_0_0_130105007_0");
        AddressDTO addressDTO = SmAreaIdUtil.getAddressDTO(csa);
        tacLogger.info("addressDTO:" + JSON.toJSONString(addressDTO));
        Long smAreaId = SmAreaIdUtil.getSmAreaId(context);
        Long userId = MapUtil.getLongWithDefault(context.getRequestParams(), "userId", 0L);
        Long index = MapUtil.getLongWithDefault(context.getRequestParams(), "index", 1L);
        Long pageSize = MapUtil.getLongWithDefault(context.getRequestParams(), "pageSize", 20L);
        dataContext.setIndex(index);
        dataContext.setPageSize(pageSize);
        //        OriginDataDTO<ItemEntity> cacheOriginDataDTO = getItemToCacheOfArea(smAreaId);
        //        if (cacheOriginDataDTO == null) {
        //tair获取推荐商品

        List<ColumnCenterDataSetItemRuleDTO> columnCenterDataSetItemRuleDTOList = tairUtil.getOriginalRecommend(
            smAreaId);
        List<Long> items = columnCenterDataSetItemRuleDTOList.stream().map(
            ColumnCenterDataSetItemRuleDTO::getItemId).collect(Collectors.toList());
        dataContext.setItems(items);
        return recommendSpi.recommendItem(this.buildRecommendRequestParam(userId, items))
            .map(recommendResponseEntityResponse -> {
                if (!recommendResponseEntityResponse.isSuccess()
                    || recommendResponseEntityResponse.getValue() == null
                    || CollectionUtils.isEmpty(recommendResponseEntityResponse.getValue().getResult())) {
                    tacLogger.info("tpp个性化排序返回异常：" + JSON.toJSONString(recommendResponseEntityResponse));
                    return new OriginDataDTO<>();
                }
                OriginDataDTO<ItemEntity> originDataDTO = convert(recommendResponseEntityResponse.getValue());
                //this.setItemToCacheOfArea(originDataDTO, smAreaId);
                return this.getItemPage(originDataDTO, dataContext);
            });
        //        } else {
        //            return Flowable.just(this.getItemPage(cacheOriginDataDTO, dataContext));
        //        }
    }

    /**
     * tpp获取个性化排序规则参数构建
     *
     * @param userId
     * @param itemIds
     * @return
     */
    private RecommendRequest buildRecommendRequestParam(Long userId, List<Long> itemIds) {
        RecommendRequest recommendRequest = new RecommendRequest();
        recommendRequest.setLogResult(true);
        recommendRequest.setUserId(userId);
        recommendRequest.setAppId(APP_ID);
        Map<String, String> params = Maps.newHashMap();
        params.put("userItemIdList", Joiner.on(",").join(itemIds));
        recommendRequest.setParams(params);
        return recommendRequest;
    }

    /**
     * 分页并做沉底处理
     *
     * @param originDataDTO
     * @param dataContext
     * @return
     */
    private OriginDataDTO<ItemEntity> getItemPage(OriginDataDTO<ItemEntity> originDataDTO, DataContext dataContext) {
        List<ItemEntity> itemEntities = LogicPageUtil.getPage(originDataDTO.getResult(), dataContext.getIndex(),
            dataContext.getPageSize());
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

}
