package com.tmall.wireless.tac.biz.processor.detail.o2o.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.cola.extension.Extension;
import com.alibaba.common.logging.Logger;
import com.alibaba.common.logging.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.metrics.StringUtils;

import com.google.common.collect.Lists;
import com.tmall.aselfcaptain.util.StackTraceUtil;
import com.tmall.tcls.gs.sdk.biz.extensions.content.vo.DefaultContentVoBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.RecTypeEnum;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecContentResultVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendContentVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendVO.DetailEvent;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailTextComponentVO.Style;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.FrontBackMapEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderContentTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 * @author: guichen
 * @Data: 2021/9/10
 * @Description:
 */
@Extension(bizId = DetailConstant.BIZ_ID,
    useCase = DetailConstant.USE_CASE_O2O,
    scenario = DetailConstant.CONTENT_SCENERIO)
@Service
public class O2ODetailContentVoBuildSdkExtPt  extends DefaultContentVoBuildSdkExtPt {

    Logger logger = LoggerFactory.getLogger(O2ODetailContentVoBuildSdkExtPt.class);
    @Override
    public SgFrameworkResponse<ContentVO> process(SgFrameworkContextContent sgFrameworkContextContent) {
        DetailRecContentResultVO response=new DetailRecContentResultVO();

        try {
            //初步convert的结果
            SgFrameworkResponse<ContentVO> contentVOSgFrameworkResponse = super.process(sgFrameworkContextContent);
            List<ContentVO> itemAndContentList = contentVOSgFrameworkResponse.getItemAndContentList();

            //入参
            DetailRecommendRequest detailRequest = DetailRecommendRequest.getDetailRequest(
                sgFrameworkContextContent.getTacContext());


            if (RecTypeEnum.RECIPE.getType().equals(detailRequest.getRecType())){
                return recipeConvert(RecTypeEnum.RECIPE.getType(),
                    itemAndContentList);
            }

        }catch (Exception e){
            //logger.error("O2ODetailContentVoBuildSdkExtPt.start.error:{}", StackTraceUtil.stackTrace(e));

        }

        return response;
    }

    private DetailRecContentResultVO recipeConvert(String scene, List<ContentVO> itemAndContentList) {

        DetailRecContentResultVO detailRecContentResultVO=new DetailRecContentResultVO();
        detailRecContentResultVO.setEnableScroll(true);
        detailRecContentResultVO.setShowArrow(true);

        //曝光埋点
        JSONObject exposureExtraParam=new JSONObject();
        List<String> scmJoin=new ArrayList<>();
        exposureExtraParam.put("scmJoin",String.join(",",scmJoin));

        detailRecContentResultVO.setExposureExtraParam(exposureExtraParam);


        List<ContentVO> recipeContents = itemAndContentList.stream()
            .filter(v -> RenderContentTypeEnum.recipeContent.getType().equals(v.getString("contentType")))
            .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(recipeContents) && recipeContents.size() > 2) {
            detailRecContentResultVO.setResult(convertResult(scene, recipeContents.subList(0, 6), scmJoin));
            return detailRecContentResultVO;
        }

        itemAndContentList.removeAll(recipeContents);
        if (CollectionUtils.isNotEmpty(itemAndContentList)) {
            detailRecContentResultVO.setResult(convertResult(scene, itemAndContentList.subList(0, 6), scmJoin));
            return detailRecContentResultVO;
        }

        return null;
    }

    private List<DetailRecommendVO> convertResult(String scene, List<ContentVO> itemAndContentList, List<String> scmJoin) {

        List<DetailRecommendVO> detailRecommendVOS = new ArrayList<>();
        for (int index = 0; index < itemAndContentList.size(); index++) {
            ContentVO contentVO = itemAndContentList.get(index);
            DetailRecommendContentVO recommendContentVO = new DetailRecommendContentVO();
            recommendContentVO.setContentId(contentVO.getLong("contentId"));
            String shortTile = contentVO.getString(FrontBackMapEnum.contentShortTitle.getFront());
            String title=StringUtils.isNotBlank(shortTile) ? shortTile : contentVO.getString("contentTitle");
            String subTitle=contentVO.getString("contentSubtitle");
            //标题
            recommendContentVO.setTitle(Lists.newArrayList(new DetailTextComponentVO(title, new Style("12", "#111111", "true"))));
            //副标题
            recommendContentVO.setSubTitle(Lists.newArrayList(new DetailTextComponentVO(subTitle, new Style("12", "#111111", "true"))));

            recommendContentVO.setImg(contentVO.getString(FrontBackMapEnum.contentPic.getFront()));
            recommendContentVO.setEvent(getEvents(scene, recommendContentVO.getContentId(),
                contentVO.getString(FrontBackMapEnum.contentCustomLink.getFront()), index + 1,
                contentVO.getString("scm")));
            detailRecommendVOS.add(recommendContentVO);

            //scm拼接
            scmJoin.add(contentVO.getString("scm"));
        }
        return detailRecommendVOS;
    }


    private static List<DetailEvent> getEvents(String scene,Long id,String jumpUrl, Integer index,String scm) {

        DetailEvent eventView1 = new DetailEvent("openUrl");

        eventView1.addFiledsParam("url", jumpUrl + "&scm=" + scm);

        DetailEvent eventView2 = new DetailEvent("userTrack");

        eventView2.addFiledsParam("page", "Page_Detail");
        eventView2.addFiledsParam("eventId","2101");
        eventView2.addFiledsParam("arg1","Page_Detail_Button-"+scene);

        Map<String, String> argsMap = new HashMap(4);
        argsMap.put("spm", "a2141.7631564."+scene+"."+index);
        argsMap.put("refer_id", String.valueOf(id));
        argsMap.put("index", String.valueOf(index));
        argsMap.put("type", scene);
        argsMap.put("scm", scm);

        eventView2.addFiledsParam("args", argsMap);

        return Lists.newArrayList(eventView1, eventView2);
    }

}
