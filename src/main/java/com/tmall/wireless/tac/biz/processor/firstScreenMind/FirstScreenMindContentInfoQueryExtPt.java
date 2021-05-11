package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import com.alibaba.cola.extension.Extension;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.impl.mc.MultiClusterTairManager;
import com.tmall.aselfcommon.model.scene.domain.TairSceneDTO;
import com.tmall.txcs.gs.framework.extensions.content.ContentInfoQueryExtPt;
import com.tmall.txcs.gs.framework.extensions.content.ContentInfoQueryRequest;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.content.ContentDTO;
import com.tmall.txcs.gs.model.model.dto.ContentEntity;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.model.spi.model.ItemInfoDTO;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.RenderErrorEnum;
import com.tmall.wireless.tac.dataservice.log.TacLoggerImpl;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
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
    TairFactorySpi tairFactorySpi;
    private static final int labelSceneNamespace = 184;

    @Override
    public Flowable<Response<Map<Long, ContentDTO>>> process(ContentInfoQueryRequest contentInfoQueryRequest) {

        tacLogger.info("***********FirstScreenMindContentInfoQueryExtPt contentInfoQueryRequest*******:"+contentInfoQueryRequest);
        /*场景详情缓存前缀*/
        String pKey = "txcs_scene_detail_v2";
        Map<Long, ContentDTO> contentDTOMap = Maps.newHashMap();
        try {
            List<ContentEntity> contentEntities = contentInfoQueryRequest.getContentEntities();

            List<String> sKeyList = new ArrayList<>();
            for (ContentEntity contentEntity : contentEntities) {
                sKeyList.add(pKey + "_" + contentEntity.getContentId());
            }
            Result<List<DataEntry>> mgetResult =tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager().mget(labelSceneNamespace, sKeyList);
            tacLogger.info("***********FirstScreenMindContentInfoQueryExtPt mgetResult*******:"+mgetResult.toString());
            if (!mgetResult.isSuccess() || CollectionUtils.isEmpty(mgetResult.getValue())) {
                return Flowable.just(Response.fail(""));
            }
            List<DataEntry> dataEntryList = mgetResult.getValue();
            Map<Long, TairSceneDTO> tairResult = Maps.newHashMap();
            //循环遍历获取结果
            dataEntryList.forEach(dataEntry -> {
                    // txcs_scene_detail_v2_2020053172349
                    Object tairKey = dataEntry.getKey();
                    String tairKeyStr = String.valueOf(tairKey);
                    String[] s = tairKeyStr.split("_");
                    String contentId = s[s.length - 1];
                    TairSceneDTO value = (TairSceneDTO) dataEntry.getValue();
                    tairResult.put(Long.valueOf(contentId), value);
            });;
            for(ContentEntity contentEntity : contentEntities){
                Long contentId = contentEntity.getContentId();
                TairSceneDTO tairSceneDTO = tairResult.get(contentId);
                /**如果内容后台返回的补全内容为空，那么把这个内容过滤掉，并且日志记录*/
                if(!tairResult.containsKey(contentId) || tairSceneDTO == null){
                    tacLogger.info("批量补全内容中心信息返回为空contentId:" + contentId +",tairResult:"+tairResult);
                    continue;
                }
                ContentDTO contentDTO = new ContentDTO();
                contentDTO.setContentId(contentId);
                contentDTO.setContentEntity(contentEntity);
                List<ItemInfoDTO> itemInfoDTOList = Lists.newArrayList();
                for(ItemEntity itemEntity : contentEntity.getItems()){
                    ItemInfoDTO itemInfoDTO = new ItemInfoDTO();
                    itemInfoDTO.setItemEntity(itemEntity);
                    itemInfoDTOList.add(itemInfoDTO);
                }
                contentDTO.setItemInfoDTOList(itemInfoDTOList);
                contentDTOMap.put(contentId,contentDTO);
            }
        }catch (Exception e){
            LOGGER.info(RenderErrorEnum.contentBatchTairExc.getCode(), RenderErrorEnum.contentBatchTairExc.getMessage());
            return Flowable.just(Response.fail(RenderErrorEnum.contentBatchTairExc.getCode()));
        }
        tacLogger.info("****FirstScreenMindContentInfoQueryExtPt contentDTOMap*****:"+contentDTOMap.toString());
        return Flowable.just(Response.success(contentDTOMap));
    }


}
