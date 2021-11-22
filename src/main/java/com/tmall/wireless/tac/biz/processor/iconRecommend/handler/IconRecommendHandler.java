package com.tmall.wireless.tac.biz.processor.iconRecommend.handler;


import com.taobao.util.CollectionUtil;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.iconRecommend.constant.ConstantValue;
import com.tmall.wireless.tac.biz.processor.iconRecommend.service.IconRecommendService;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public static final int SCENE_NUMBER = 4;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<ContentVO>>> executeFlowable(Context context) throws Exception {

        Flowable<TacResult<SgFrameworkResponse<ContentVO>>> classifier = iconRecommendService.recommend(context, ConstantValue.CLASSIFIER_WORD);

        Flowable<TacResult<SgFrameworkResponse<ContentVO>>> scene = iconRecommendService.recommend(context, ConstantValue.SCENE_WORD);

        return Flowable.zip(classifier, scene, (c, s) -> {

            List<ContentVO> classifierContentVOList = c.getData().getItemAndContentList();
            List<ContentVO> sceneContentVOList = s.getData().getItemAndContentList();

            if (CollectionUtil.isEmpty(classifierContentVOList)) {
                return TacResult.newResult(new SgFrameworkResponse<>());
            }
            if (CollectionUtil.isEmpty(sceneContentVOList)) {
                logger.info("[IconRecommendHandler] 场景词推荐为空！");
                HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_ICON_RECOMMEND_SCENE)
                        .kv("[IconRecommendHandler] ", "场景词推荐为空！")
                        .info();
            }

            // 少于4个，不下发
            if (classifierContentVOList.size() + sceneContentVOList.size() < SCENE_NUMBER) {
                return TacResult.newResult(new SgFrameworkResponse<>());
            }

            // 4分类 1场景
            if (sceneContentVOList.size() == 1 && classifierContentVOList.size() == SCENE_NUMBER) {
                classifierContentVOList.set(classifierContentVOList.size() - 1, sceneContentVOList.get(0));
            }
            // 3分类 1场景
            else if (classifierContentVOList.size() == 3) {
                classifierContentVOList.addAll(sceneContentVOList);
            }

            logger.info("[IconRecommendHandler] Size of content: " + classifierContentVOList.size());
            // 取第1个物品照片作为icon图片
            for (ContentVO contentVO : classifierContentVOList) {
                contentVO.put("iconPic", contentVO.getJSONArray("items").getJSONObject(0).getString("itemImg"));
            }
            c.getData().setItemAndContentList(classifierContentVOList);
            return c;
        });
    }
}
