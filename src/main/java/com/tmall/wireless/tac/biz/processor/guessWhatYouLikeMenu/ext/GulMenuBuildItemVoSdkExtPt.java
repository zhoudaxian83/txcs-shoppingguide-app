package com.tmall.wireless.tac.biz.processor.guessWhatYouLikeMenu.ext;

import com.tmall.aselfcaptain.util.StackTraceUtil;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.biz.extensions.item.vo.DefaultBuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemEntity;
import com.tmall.tcls.gs.sdk.framework.model.context.ItemInfoDTO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextContent;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;

import javax.annotation.Resource;

/**
 * @author Yushan
 * @date 2021/9/15 11:40 下午
 * VO加个字段：crowdId
 */
@SdkExtension(
    bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.CNXH_MENU_FEEDS
)
public class GulMenuBuildItemVoSdkExtPt extends DefaultBuildItemVoSdkExtPt implements BuildItemVoSdkExtPt {

    @Resource
    TacLogger logger;

    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        Response<ItemEntityVO> result = super.process(buildItemVoRequest);
        //        logger.info("GulMenuBuildItemVoSdkExtPt");
        try {
            ItemEntityVO itemEntityVO = result.getValue();
            ItemInfoDTO itemInfoDTO = buildItemVoRequest.getItemInfoDTO();
            if (itemInfoDTO != null && itemInfoDTO.getItemEntity() != null) {
                ItemEntity itemEntity = itemInfoDTO.getItemEntity();
                logger.info("nnn" + itemEntity.getCrowdId());
                itemEntityVO.put("crowdId", itemEntity.getCrowdId());
            }
            result.setValue(itemEntityVO);
        } catch (Exception e) {
            logger.error("Fail to add crowdId to items.", e);
            HadesLogUtil.stream(ScenarioConstantApp.CNXH_MENU_FEEDS)
                .kv("GulMenuBuildItemVoSdkExtPt", "error")
                .kv("Fail to add crowdId to items", StackTraceUtil.stackTrace(e))
                .error();
        }
        return result;
    }
}
