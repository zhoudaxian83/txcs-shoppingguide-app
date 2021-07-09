package com.tmall.wireless.tac.biz.processor.mmc.handler;

import com.taobao.freshx.homepage.client.domain.MaterialDO;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler;
import io.reactivex.Flowable;

/**
 * 买买菜商品渲染
 * @author haixiao.zhang
 * @date 2021/7/9
 */
public class MmcItemMergeHandler implements TacReactiveHandler<MaterialDO> {

    @Override
    public Flowable<TacResult<MaterialDO>> executeFlowable(Context context) throws Exception {
        return null;
    }

}
