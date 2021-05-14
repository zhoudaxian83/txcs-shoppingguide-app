package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.model.model.dto.RecommendResponseEntity;
import com.tmall.txcs.gs.model.model.dto.tpp.RecommendItemEntityDTO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.txcs.gs.spi.recommend.RecommendSpi;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
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

    @Autowired
    TairFactorySpi tairFactorySpi;
    private static final int labelSceneNamespace = 184;

/*    @Autowired
    TodayCrazyLimitFacade todayCrazyLimitFacade;*/

    @Autowired
    RecommendSpi recommendSpi;

    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem context) {
        tacLogger.info("WuZheTianOriginDataItemQueryExtPt");
        tacLogger.info("[WuZheTianOriginDataItemQueryExtPt] context={}" + JSON.toJSONString(context));
        OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();
        originDataDTO.setResult(buildItemList());

        RecommendRequest recommendRequest = new RecommendRequest();
        Flowable<Response<RecommendResponseEntity<RecommendItemEntityDTO>>> responseFlowable = recommendSpi
            .recommendItem(recommendRequest);

       /* ItemLimitInfoQuery var1 = new ItemLimitInfoQuery();
        //var1.setItemIdList(context.g);
        var1.setUserId(context.getUserDO().getUserId());
        ItemLimitResult itemLimitResult = todayCrazyLimitFacade.query(var1);*/

        List<String> sKeyList = new ArrayList<>();
        sKeyList.add("wuZheTian_HD_pre");
        sKeyList.add("wuZheTian_HB_pre");
        sKeyList.add("wuZheTian_HN_pre");
        sKeyList.add("wuZheTian_HZ_pre");
        sKeyList.add("wuZheTian_XN_pre");
        Result<List<DataEntry>> mgetResult = tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager()
            .mget(labelSceneNamespace, sKeyList);
        tacLogger.info("[WuZheTianOriginDataItemQueryExtPt] mgetResult=" + JSON.toJSONString(mgetResult));
        return Flowable.just(originDataDTO);
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
