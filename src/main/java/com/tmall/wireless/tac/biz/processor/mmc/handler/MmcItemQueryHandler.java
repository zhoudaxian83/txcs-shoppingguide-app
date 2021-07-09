package com.tmall.wireless.tac.biz.processor.mmc.handler;

import com.taobao.freshx.homepage.client.domain.ItemRecallModeDO;
import com.taobao.poi2.client.result.StoreResult;
import com.tmall.txcs.gs.spi.recommend.AldSpi;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 买买菜商品查询
 * @author haixiao.zhang
 * @date 2021/7/9
 */
public class MmcItemQueryHandler implements TacReactiveHandler<ItemRecallModeDO> {


    @Autowired
    private AldSpi aldSpi;

    @Override
    public Flowable<TacResult<ItemRecallModeDO>> executeFlowable(Context context) throws Exception {

        context.getParams().get("userId");
        context.getParams().get("source");
        List<StoreResult> storeResultList = (List<StoreResult>)context.getParams().get("stores");

        ItemRecallModeDO itemRecallModeDO = new ItemRecallModeDO();

        return Flowable.just(TacResult.newResult(itemRecallModeDO));
    }
}
