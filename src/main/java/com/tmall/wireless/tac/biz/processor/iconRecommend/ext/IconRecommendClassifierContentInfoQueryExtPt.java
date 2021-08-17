package com.tmall.wireless.tac.biz.processor.iconRecommend.ext;

import com.alibaba.cola.extension.Extension;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.gs.framework.extensions.content.ContentInfoQueryExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.content.ContentInfoDTO;
import com.tmall.txcs.gs.model.model.dto.ContentEntity;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import io.reactivex.Flowable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Yushan
 * @date 2021/8/9 8:47 下午
 */
@Extension(bizId = ScenarioConstant.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstant.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENARIO_ICON_RECOMMEND_CLASSIFIER
)
@Service
public class IconRecommendClassifierContentInfoQueryExtPt implements ContentInfoQueryExtPt {
    @Override
    public Flowable<Response<Map<Long, ContentInfoDTO>>> process(SgFrameworkContextContent sgFrameworkContextContent) {

        Map<Long, ContentInfoDTO> resMap = Maps.newHashMap();

        List<ContentEntity> contentEntities  = Optional.of(sgFrameworkContextContent)
                .map(SgFrameworkContextContent::getContentEntityOriginDataDTO)
                .map(OriginDataDTO::getResult)
                .orElse(Lists.newArrayList());

        for (ContentEntity contentEntity : contentEntities) {
            Map<String, Object> contentInfo = Maps.newHashMap();
            Long contentId = contentEntity.getContentId();
            contentInfo.put("contentId", contentId);
            contentInfo.put("uniqueId", contentEntity.getUnique_id());
            contentInfo.put("trackPoint", contentEntity.getTrack_point());
            contentInfo.put("Rn", contentEntity.getRn());
            contentInfo.put("ext", contentEntity.getExt());

            ContentInfoDTO contentInfoDTO = new ContentInfoDTO();
            contentInfoDTO.setContentInfo(contentInfo);
            resMap.put(contentId, contentInfoDTO);
        }
        return Flowable.just(Response.success(resMap));

//        Map<Long, ContentInfoDTO> resMap = Maps.newHashMap();
//        Map<String, Object> contentInfo = Maps.newHashMap();
//        contentInfo.put("url","111");
//        ContentInfoDTO contentInfoDTO = new ContentInfoDTO();
//        contentInfoDTO.setContentInfo(contentInfo);
//        resMap.put(5233L,contentInfoDTO);
//        resMap.put(322385L,contentInfoDTO);
//        return Flowable.just(Response.success(resMap));
    }
}

