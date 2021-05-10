package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import com.alibaba.cola.extension.Extension;
import com.google.common.collect.Maps;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.impl.mc.MultiClusterTairManager;
import com.tmall.txcs.gs.framework.extensions.content.ContentInfoQueryExtPt;
import com.tmall.txcs.gs.framework.extensions.content.ContentInfoQueryRequest;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.content.ContentDTO;
import com.tmall.txcs.gs.model.model.dto.ContentEntity;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderErrorEnum;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import io.reactivex.Flowable;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
@Service
public class FirstScreenMindContentInfoQueryExtPt implements ContentInfoQueryExtPt {

    Logger LOGGER = LoggerFactory.getLogger(FirstScreenMindContentInfoQueryExtPt.class);

    @Autowired
    TacLoggerImpl tacLogger;

    @Resource
    private MultiClusterTairManager multiClusterTairManager;

    private static final int labelSceneNamespace = 184;

    @Override
    public Flowable<Response<Map<Long, ContentDTO>>> process(ContentInfoQueryRequest contentInfoQueryRequest) {

        tacLogger.info("***********FirstScreenMindContentInfoQueryExtPt contentInfoQueryRequest*******:"+contentInfoQueryRequest);
        /*场景详情缓存前缀*/
        String sceneLabelDetail = "txcs_scene_detail_v1";
        String pKey = sceneLabelDetail;
        Map<Long, ContentDTO> contentDTOMap = Maps.newHashMap();
        try {
            List<ContentEntity> contentEntities = contentInfoQueryRequest.getContentEntities();
            List<String> sKeyList = new ArrayList<>();
            for (ContentEntity contentEntity : contentEntities) {
                sKeyList.add(String.valueOf(contentEntity.getContentId()));
            }
            tacLogger.info("***********FirstScreenMindContentInfoQueryExtPt labelSceneNamespace*******:"+labelSceneNamespace);
            tacLogger.info("***********FirstScreenMindContentInfoQueryExtPt pKey*******:"+pKey);
            tacLogger.info("***********FirstScreenMindContentInfoQueryExtPt sKeyList*******:"+sKeyList);
            Result<Map<Object, Result<DataEntry>>> labelSceneResult =
                    multiClusterTairManager.prefixGets(labelSceneNamespace, pKey, sKeyList);
            tacLogger.info("***********FirstScreenMindContentInfoQueryExtPt labelSceneResult*******:"+labelSceneResult);
            if (!labelSceneResult.isSuccess()) {
                return Flowable.just(Response.fail(""));
            }
            Map<Object, Result<DataEntry>> resultMap = labelSceneResult.getValue();
            //循环遍历获取结果
            for (Object sKey : resultMap.keySet()) {
                Result<DataEntry> result = resultMap.get(sKey);
                if (!result.isSuccess()) {
                    LOGGER.info(RenderErrorEnum.contentSingleTairFail.getCode(), RenderErrorEnum.contentSingleTairFail.getMessage());
                    return Flowable.just(Response.fail(RenderErrorEnum.contentSingleTairFail.getCode()));
                }
                DataEntry dataEntry = result.getValue();
                if (dataEntry == null || dataEntry.getValue() == null) {
                    LOGGER.info(RenderErrorEnum.contentSingleTairValueNull.getCode(), RenderErrorEnum.contentSingleTairValueNull.getMessage());
                    return Flowable.just(Response.fail(RenderErrorEnum.contentSingleTairValueNull.getCode()));
                }
                //单个内容类型转换
                ContentDTO contentDTO = new ContentDTO();
                contentDTO.setContentId((Long) sKey);
                contentDTO.setContentInfo((Map<String, Object>) dataEntry.getValue());
                contentDTOMap.put(((Long) sKey),contentDTO);
            }
            tacLogger.info("***********FirstScreenMindContentInfoQueryExtPt contentDTOMap*******:"+contentDTOMap);
            //结果判空
            if (MapUtils.isEmpty(contentDTOMap)) {
                LOGGER.info(RenderErrorEnum.contentBatchTairValueNull.getCode(), RenderErrorEnum.contentBatchTairValueNull.getMessage());
                return Flowable.just(Response.fail(RenderErrorEnum.contentBatchTairValueNull.getCode()));
            }
        }catch (Exception e){
            LOGGER.info(RenderErrorEnum.contentBatchTairExc.getCode(), RenderErrorEnum.contentBatchTairExc.getMessage());
            return Flowable.just(Response.fail(RenderErrorEnum.contentBatchTairExc.getCode()));
        }
        tacLogger.info("****FirstScreenMindContentInfoQueryExtPt contentDTOMap*****:"+contentDTOMap);
        return Flowable.just(Response.success(contentDTOMap));
    }


}
