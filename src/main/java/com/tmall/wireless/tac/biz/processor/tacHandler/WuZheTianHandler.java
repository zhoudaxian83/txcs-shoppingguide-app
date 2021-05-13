package com.tmall.wireless.tac.biz.processor.tacHandler;

import java.util.Map;

import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.FirstPageBannerItemInfoScene;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model.BannerVO;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/13 18:02
 */
@Component
public class WuZheTianHandler extends RpmReactiveHandler<Map<String, BannerVO>> {

/*    @Autowired
    GulSubTabScene gulSubTabScene;*/

    @Autowired
    FirstPageBannerItemInfoScene firstPageBannerItemInfoScene;

    @Override
    public Flowable<TacResult<Map<String, BannerVO>>> executeFlowable(Context context) throws Exception {
        return firstPageBannerItemInfoScene.recommend(context);
    }
}
