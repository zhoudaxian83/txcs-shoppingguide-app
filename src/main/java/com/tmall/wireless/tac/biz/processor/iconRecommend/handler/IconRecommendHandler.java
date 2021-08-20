package com.tmall.wireless.tac.biz.processor.iconRecommend.handler;

import com.alibaba.fastjson.JSONArray;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.iconRecommend.constant.ConstantValue;
import com.tmall.wireless.tac.biz.processor.iconRecommend.service.IconRecommendService;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yushan
 * @date 2021/8/9 2:26 下午
 */
@Component
public class IconRecommendHandler extends RpmReactiveHandler<SgFrameworkResponse<ContentVO>> {

    @Autowired
    TacLogger logger;

    @Autowired
    IconRecommendService iconRecommendService;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<ContentVO>>> executeFlowable(Context context) throws Exception {

        Flowable<TacResult<SgFrameworkResponse<ContentVO>>> classifier = iconRecommendService.recommend(context, ConstantValue.CLASSIFIER_WORD);

        Flowable<TacResult<SgFrameworkResponse<ContentVO>>> scene = iconRecommendService.recommend(context, ConstantValue.SCENE_WORD);

        return Flowable.zip(classifier, scene, (c, s) -> {
            List<ContentVO> classifierContentVOList = c.getData().getItemAndContentList();
            List<ContentVO> sceneContentVOList = s.getData().getItemAndContentList();
            List<ContentVO> res = new ArrayList<>();

            // 场景词item数量少于六个，分类词打底
            if (sceneContentVOList != null && sceneContentVOList.size() > 0 && classifierContentVOList != null && classifierContentVOList.size() >= 3) {
                if (sceneContentVOList.get(0).getJSONArray("items") != null && sceneContentVOList.get(0).getJSONArray("items").size() < 6) {
                    res = classifierContentVOList;
                } else {
                    res.addAll(classifierContentVOList.subList(0, 3));
                    res.addAll(sceneContentVOList);
                }
            }
            // 总数量少于4个，传空下发
            if (!res.isEmpty() && res.size() <= 4) {
                return TacResult.newResult(new SgFrameworkResponse<ContentVO>());
            }

            // 取第1个物品照片作为icon图片
            for (ContentVO contentVO : res) {
                if (contentVO == null || contentVO.getJSONArray("items") == null || contentVO.getJSONArray("items").size() < 6) {
                    return TacResult.newResult(new SgFrameworkResponse<ContentVO>());
                }
                contentVO.put("iconPic", contentVO.getJSONArray("items").getJSONObject(0).getString("itemImg"));
            }
            SgFrameworkResponse<ContentVO> sgFrameworkResponse = new SgFrameworkResponse<>();
            sgFrameworkResponse.setItemAndContentList(res);
            return TacResult.newResult(sgFrameworkResponse);
        });
    }
}
