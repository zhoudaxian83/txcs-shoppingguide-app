package com.tmall.wireless.tac.biz.processor.chaohaotou.ext;

import com.ali.com.google.common.base.Joiner;
import com.ali.unit.rule.util.lang.CollectionUtils;
import com.alibaba.cola.extension.Extension;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import com.tmall.wireless.tac.biz.processor.chaohaotou.constant.Constant;
import com.tmall.wireless.tac.biz.processor.chaohaotou.model.DataContext;
import com.tmall.wireless.tac.biz.processor.chaohaotou.service.CommercialFeedsService;
import com.tmall.wireless.tac.biz.processor.chaohaotou.utils.LogicPageUtil;
import com.tmall.wireless.tac.biz.processor.chaohaotou.utils.SmAreaIdUtil;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import io.reactivex.Flowable;
import org.apache.commons.lang3.tuple.Pair;
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
        scenario = ScenarioConstantApp.CHAO_HAO_TOU)
@Service
public class ChaoHaoTouDataItemQueryExtPt implements OriginDataItemQueryExtPt {

    @Autowired
    TairUtil tairUtil;

    @Autowired
    RecommendSpi recommendSpi;

    @Autowired
    CommercialFeedsService commercialFeedsService;

    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem context) {
        DataContext dataContext = new DataContext();
        Long smAreaId = SmAreaIdUtil.getSmAreaId(context);
        Long userId = MapUtil.getLongWithDefault(context.getRequestParams(), "userId", 0L);
        Long index = MapUtil.getLongWithDefault(context.getRequestParams(), "index", 1L);
        Long pageSize = MapUtil.getLongWithDefault(context.getRequestParams(), "pageSize", 20L);
        dataContext.setIndex(index);
        dataContext.setPageSize(pageSize);
        commercialFeedsService.getCommercialFeeds(context);
        List<Long> items = Lists.newArrayList();
        return recommendSpi.recommendItem(this.buildRecommendRequestParam(userId, items))
                .map(recommendResponseEntityResponse -> {
                    if (!recommendResponseEntityResponse.isSuccess()
                            || recommendResponseEntityResponse.getValue() == null
                            || CollectionUtils.isEmpty(recommendResponseEntityResponse.getValue().getResult())) {
                        return new OriginDataDTO<>();
                    }
                    OriginDataDTO<ItemEntity> originDataDTO = convert(recommendResponseEntityResponse.getValue());
                    return this.getItemPage(originDataDTO, dataContext);
                });
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
        recommendRequest.setAppId(Constant.APP_ID);
        Map<String, String> params = Maps.newHashMap();
        params.put("userItemIdList", Joiner.on(",").join(itemIds));
        recommendRequest.setParams(params);
        return recommendRequest;
    }

    /**
     * 分页
     *
     * @param originDataDTO
     * @param dataContext
     * @return
     */
    private OriginDataDTO<ItemEntity> getItemPage(OriginDataDTO<ItemEntity> originDataDTO, DataContext dataContext) {
        Pair<Boolean, List<ItemEntity>> pair = LogicPageUtil.getPage(originDataDTO.getResult(), dataContext.getIndex(),
                dataContext.getPageSize());
        List<ItemEntity> itemEntities = pair.getRight();
        originDataDTO.setHasMore(pair.getLeft());
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
