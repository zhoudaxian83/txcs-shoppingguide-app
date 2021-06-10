package com.tmall.wireless.tac.biz.processor.newproduct.ext;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.tmall.txcs.gs.framework.extensions.itemdatapost.ItemInfoPostProcessorExtPt;
import com.tmall.txcs.gs.framework.extensions.itemdatapost.ItemInfoPostProcessorResp;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.ItemGroup;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.framework.support.itemInfo.ItemInfoGroupResponse;
import com.tmall.txcs.gs.model.Response;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author haixiao.zhang
 * @date 2021/6/10
 */
@Extension(bizId = ScenarioConstant.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstant.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_SHANG_XIN_CONTENT)
@Service
public class SxlItemInfoPostProcessorExtPt implements ItemInfoPostProcessorExtPt {

    @Autowired
    TacLogger tacLogger;

    @Override
    public Response<ItemInfoPostProcessorResp> process(SgFrameworkContextItem sgFrameworkContextItem) {

        Map<ItemGroup, ItemInfoGroupResponse> itemGroupItemInfoGroupResponseMap = sgFrameworkContextItem
            .getItemInfoGroupResponseMap();

        SgFrameworkResponse<EntityVO> entityVOgFrameworkResponse = sgFrameworkContextItem.getEntityVOSgFrameworkResponse();
        List<EntityVO> list = entityVOgFrameworkResponse.getItemAndContentList();
        tacLogger.info("SxlItemInfoPostProcessorExtPt list:{}"+JSON.toJSONString(list));
        return Response.success(new ItemInfoPostProcessorResp());
    }
}
