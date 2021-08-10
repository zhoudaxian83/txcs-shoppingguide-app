package com.tmall.wireless.tac.biz.processor.iconRecommend.ext;

import com.alibaba.cola.extension.Extension;
import com.google.common.collect.Maps;
import com.tmall.txcs.gs.framework.extensions.content.ContentInfoQueryExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.content.ContentInfoDTO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import io.reactivex.Flowable;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Yushan
 * @date 2021/8/9 8:47 下午
 */
@Extension(bizId = ScenarioConstant.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstant.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENARIO_ICON_RECOMMEND_CLASSIFIER
)
@Service
public class IconRecommendContentInfoQueryExtPt implements ContentInfoQueryExtPt {
    @Override
    public Flowable<Response<Map<Long, ContentInfoDTO>>> process(SgFrameworkContextContent sgFrameworkContextContent) {
        ContentInfoDTO contentInfoDTO = new ContentInfoDTO();
        Map<Long, ContentInfoDTO> res = Maps.newHashMap();
        res.put(77777L, contentInfoDTO);
        return Flowable.just(Response.success(res));
    }
}
