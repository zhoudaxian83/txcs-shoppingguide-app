package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.tmall.txcs.biz.supermarket.extpt.origindata.ConvertUtil;
import com.tmall.txcs.gs.framework.extensions.excutor.SgExtensionExecutor;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.item.O2oType;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.model.model.dto.RecommendResponseEntity;
import com.tmall.txcs.gs.model.model.dto.tpp.RecommendItemEntityDTO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.txcs.gs.spi.recommend.RecommendSpi;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
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

    @Autowired
    TairFactorySpi tairFactorySpi;
    private static final int labelSceneNamespace = 184;

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
        tacLogger.info("[WuZheTianOriginDataItemQueryExtPt] context={}" + JSON.toJSONString(context));
        OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();
        originDataDTO.setResult(buildItemList());

        //获取商品排期列表
        List<String> sKeyList = new ArrayList<>();
        sKeyList.add("wuZheTian_HD_pre");
        sKeyList.add("wuZheTian_HB_pre");
        sKeyList.add("wuZheTian_HN_pre");
        sKeyList.add("wuZheTian_HZ_pre");
        sKeyList.add("wuZheTian_XN_pre");
        Result<List<DataEntry>> mgetResult = tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager()
            .mget(labelSceneNamespace, sKeyList);
        tacLogger.info("[WuZheTianOriginDataItemQueryExtPt] mgetResult=" + JSON.toJSONString(mgetResult));

        //tpp获取个性化排序规则
        RecommendRequest recommendRequest = new RecommendRequest();
        Flowable<Response<RecommendResponseEntity<RecommendItemEntityDTO>>> responseFlowable = recommendSpi
            .recommendItem(recommendRequest);

        //获取限购信息
        //ItemLimitInfoQuery itemLimitInfoQuery = new ItemLimitInfoQuery();
        //itemLimitInfoQuery.setUserId(0L);
        //itemLimitInfoQuery.setItemIdList(Arrays.asList(600819862645L, 623789407071L));
        //ItemLimitResult itemLimitResult = todayCrazyLimitFacade.query(itemLimitInfoQuery);
        //
        //tacLogger.info("[WuZheTianOriginDataItemQueryExtPt] itemLimitResult=" + JSON.toJSONString(itemLimitResult));

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

    private List<ItemEntity> buildItemList() {
        List<ItemEntity> result = Lists.newArrayList();
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setItemId(123L);
        itemEntity.setO2oType("TEST");
        result.add(itemEntity);

        ItemEntity itemEntity2 = new ItemEntity();
        itemEntity2.setItemId(12L);
        itemEntity2.setO2oType("TEST2");
        result.add(itemEntity2);
        return result;
    }
}
