package com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo;

import com.taobao.pandora.pandolet.annotation.Service;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model.BannerVO;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by yangqing.byq on 2021/4/6.
 */
@Component
public class FirstPageBannerItemInfoHandler extends RpmReactiveHandler<Map<String, BannerVO>>  {

    @Autowired
    FirstPageBannerItemInfoScene firstPageBannerItemInfoScene;

    @Override
    public Flowable<TacResult<Map<String, BannerVO>>> executeFlowable(Context context) throws Exception {
        return firstPageBannerItemInfoScene.recommend(context);
    }
}
