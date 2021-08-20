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
import java.util.stream.Collectors;

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

            // 商品数量过滤
            classifierContentVOList = classifierContentVOList.stream()
                    .filter(contentVO -> contentVO.getJSONArray("items").size() >= 6)
                    .collect(Collectors.toList());
            logger.info("[IconRecommendHandler] Size of classifier content: " + classifierContentVOList.size());
            sceneContentVOList = sceneContentVOList.stream()
                    .filter(contentVO -> contentVO.getJSONArray("items").size() >= 6)
                    .collect(Collectors.toList());
            logger.info("[IconRecommendHandler] Size of scene content: " + sceneContentVOList.size());

            // 少于4个，不下发
            if (classifierContentVOList.size() + sceneContentVOList.size() < 4) {
                return TacResult.newResult(new SgFrameworkResponse<>());
            }

            // 4分类 1场景
            if (sceneContentVOList.size() == 1 && classifierContentVOList.size() == 4) {
                classifierContentVOList.set(4, sceneContentVOList.get(0));
            }
            // 3分类 1场景
            else if (classifierContentVOList.size() == 3) {
                classifierContentVOList.addAll(sceneContentVOList);
            }

            // 取第1个物品照片作为icon图片
            for (ContentVO contentVO : classifierContentVOList) {
                contentVO.put("iconPic", contentVO.getJSONArray("items").getJSONObject(0).getString("itemImg"));
            }
            return c;
        });
    }
}
