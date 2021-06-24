package com.tmall.wireless.tac.biz.processor.o2obd.handler;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.google.common.collect.Lists;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.wireless.tac.biz.processor.o2obd.service.O2oBangdanService;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author haixiao.zhang
 * @date 2021/6/22
 */
@Service
public class O2oBangdanHandler extends TacReactiveHandler4Ald {

    @Autowired
    O2oBangdanService o2oBangdanService;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald)
        throws Exception {

        return o2oBangdanService.recommend(requestContext4Ald).map(response->{
            List<GeneralItem> generalItemList = Lists.newArrayList();
            List<ContentVO> list = response.getData().getItemAndContentList();
            list.forEach(contentVO -> {
                GeneralItem generalItem = new GeneralItem();
                contentVO.keySet().forEach(key->{
                    generalItem.put(key,contentVO.get(key));
                });
                generalItemList.add(generalItem);
            });
            return generalItemList;
        }).map(TacResult::newResult)
        .onErrorReturn((r -> TacResult.errorResult("")));

    }
}