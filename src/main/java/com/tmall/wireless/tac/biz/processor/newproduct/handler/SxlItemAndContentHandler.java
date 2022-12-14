package com.tmall.wireless.tac.biz.processor.newproduct.handler;

import com.alibaba.fastjson.JSON;

import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.newproduct.service.SxlContentRecService;
import com.tmall.wireless.tac.biz.processor.newproduct.service.SxlItemRecService;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author haixiao.zhang
 * @date 2021/6/8
 */
public class SxlItemAndContentHandler extends RpmReactiveHandler<SgFrameworkResponse<EntityVO>> {


    @Autowired
    private SxlItemRecService sxlItemRecService;

    @Autowired
    TacLogger tacLogger;

    @Autowired
    private SxlContentRecService sxlContentRecService;


    @Override
    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> executeFlowable(Context context) throws Exception{


        Flowable<TacResult<SgFrameworkResponse<ContentVO>>> content = sxlContentRecService.recommend(context);

        HadesLogUtil.stream(ScenarioConstantApp.SCENARIO_SHANG_XIN_CONTENT)
            .kv("SxlItemAndContentHandler","executeFlowable")
            .kv("content",JSON.toJSONString(content))
            .info();

        Flowable<TacResult<SgFrameworkResponse<EntityVO>>> item = sxlItemRecService.recommend(context);

        return Flowable.zip(content,item, (contentInfo, itemInfo) -> mergeContentAndItem(contentInfo, itemInfo,context));

    }


    private TacResult<SgFrameworkResponse<EntityVO>>  mergeContentAndItem(TacResult<SgFrameworkResponse<ContentVO>> contentInfo,
                                                           TacResult<SgFrameworkResponse<EntityVO>> itemInfo,Context context) {


        if(CollectionUtils.isNotEmpty(contentInfo.getData().getItemAndContentList())){

            List<ContentVO> contentList = contentInfo.getData().getItemAndContentList();

            contentList = contentList.stream().sorted(Comparator.comparingInt(e->e.getIntValue("position"))).collect(Collectors.toList());

            List<EntityVO> entityVOList = itemInfo.getData().getItemAndContentList();
            contentList.forEach(e->{
                EntityVO entityVO = new EntityVO();
                Integer position = (Integer)e.get("position");
                int index = Integer.parseInt(MapUtil.getStringWithDefault(context.getParams(), RequestKeyConstantApp.INDEX, "0"));
                if(position > index){
                    position = position-index;
                }
                if(position < entityVOList.size()){
                    entityVO.put("banner",e);
                    entityVOList.add(position-1,entityVO);
                }

            });
        }
        return itemInfo;

    }
}
