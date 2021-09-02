package com.tmall.wireless.tac.biz.processor.icon.hander;

import com.tmall.aself.shoppingguide.client.cat.model.LabelDTO;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2RecommendService;
import com.tmall.wireless.tac.biz.processor.icon.level2.Level2Request;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IconLevel1Handler extends RpmReactiveHandler<List<LabelDTO>> {

    @Autowired
    Level2RecommendService level2RecommendService;
    @Override
    public Flowable<TacResult<List<LabelDTO>>> executeFlowable(Context context) throws Exception {
        Level2Request level2Request = new Level2Request();
        level2Request.setLevel1Id(Optional.ofNullable(context.get("iconType")).map(Object::toString).orElse(""));
        return level2RecommendService.recommend(level2Request, context).map(TacResult::newResult);
    }
}
