package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.impl.mc.MultiClusterTairManager;
import com.tmall.aselfcommon.model.gcs.domain.GcsTairSceneDTO;
import com.tmall.txcs.gs.framework.extensions.content.ContentInfoQueryExtPt;
import com.tmall.txcs.gs.framework.extensions.content.ContentInfoQueryRequest;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.content.ContentDTO;
import com.tmall.txcs.gs.model.model.dto.ContentEntity;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderErrorEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderCheckUtil;
import io.reactivex.Flowable;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FSMindContentInfoQueryExtPt implements ContentInfoQueryExtPt {

    Logger LOGGER = LoggerFactory.getLogger(FSMindContentInfoQueryExtPt.class);

    @Resource
    private MultiClusterTairManager multiClusterTairManager;

    private static final int labelSceneNamespace = 184;

    @Override
    public Flowable<Response<Map<Long, ContentDTO>>> process(ContentInfoQueryRequest contentInfoQueryRequest) {
        /*场景详情缓存前缀*/
        String sceneLabelDetail = "txcs_scene_detail_v1";
        String pKey = sceneLabelDetail;
        try {
            List<ContentEntity> contentEntities = contentInfoQueryRequest.getContentEntities();
            List<String> sKeyList = new ArrayList<>();
            for (ContentEntity contentEntity : contentEntities) {
                sKeyList.add(String.valueOf(contentEntity.getContentId()));
            }
            Result<Map<Object, Result<DataEntry>>> labelSceneResult =
                    multiClusterTairManager.prefixGets(labelSceneNamespace, pKey, sKeyList);
            if (!labelSceneResult.isSuccess()) {
                return Flowable.just(Response.fail(""));
            }
            Map<Object, Result<DataEntry>> resultMap = labelSceneResult.getValue();
            //循环遍历获取结果
            Map<String, GcsTairSceneDTO> tairSceneDTOMap = new HashMap<>();
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
                @SuppressWarnings("unchecked")
                GcsTairSceneDTO labelSceneContentInfo = (GcsTairSceneDTO) dataEntry.getValue();
                if (RenderCheckUtil.objectEmpty(labelSceneContentInfo)) {
                    LOGGER.info(RenderErrorEnum.contentSingleContentNull.getCode(), RenderErrorEnum.contentSingleContentNull.getMessage());
                    return Flowable.just(Response.fail(RenderErrorEnum.contentSingleContentNull.getCode()));
                }
                tairSceneDTOMap.put(String.valueOf(sKey), labelSceneContentInfo);
            }
            //结果判空
            if (MapUtils.isEmpty(tairSceneDTOMap)) {
                LOGGER.info(RenderErrorEnum.contentBatchTairValueNull.getCode(), RenderErrorEnum.contentBatchTairValueNull.getMessage());
                return Flowable.just(Response.fail(RenderErrorEnum.contentBatchTairValueNull.getCode()));
            }
        }catch (Exception e){
            LOGGER.info(RenderErrorEnum.contentBatchTairExc.getCode(), RenderErrorEnum.contentBatchTairExc.getMessage());
            return Flowable.just(Response.fail(RenderErrorEnum.contentBatchTairExc.getCode()));
        }
        return Flowable.just(Response.fail(""));
    }


}
