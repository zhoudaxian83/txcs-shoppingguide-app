package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.tmall.aself.shoppingguide.client.todaycrazyv2.TodayCrazyLimitFacade;
import com.tmall.aself.shoppingguide.client.todaycrazyv2.query.ItemLimitInfoQuery;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.biz.context.UserControlParams;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model.BannerItemDTO;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.uitl.BannerUtil;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
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
    TairFactorySpi tairFactorySpi;
    private static final int labelSceneNamespace = 184;

    @Resource
    private TodayCrazyLimitFacade todayCrazyLimitFacade;

    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem context) {
        tacLogger.info("WuZheTianOriginDataItemQueryExtPt");
        tacLogger.info("[WuZheTianOriginDataItemQueryExtPt] SgFrameworkContextItem={}" + JSON.toJSONString(SgFrameworkContextItem));
        UserControlParams userControlParams = context.getUserControlParams()
        OriginDataDTO<ItemEntity> originDataDTO = new OriginDataDTO<>();
        ItemLimitInfoQuery var1 = new ItemLimitInfoQuery();
        var1.setItemIdList();
        var1.setUserId(userControlParams.getid);
        ItemLimitResult itemLimitResult = todayCrazyLimitFacade.query(var1)
        originDataDTO.setResult(buildItemList());
        List<String> sKeyList = new ArrayList<>();
        sKeyList.add("test");
        Result<List<DataEntry>> mgetResult = tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager()
            .mget(labelSceneNamespace, sKeyList);
        tacLogger.info("[WuZheTianOriginDataItemQueryExtPt] originDataDTO={}" + JSON.toJSONString(originDataDTO));
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
