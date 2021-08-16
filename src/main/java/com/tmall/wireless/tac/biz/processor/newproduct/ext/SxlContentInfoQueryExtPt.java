package com.tmall.wireless.tac.biz.processor.newproduct.ext;

import com.alibaba.cola.extension.Extension;
import com.google.common.collect.Maps;
import com.tmall.txcs.gs.framework.extensions.content.ContentInfoQueryExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.content.ContentInfoDTO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import io.reactivex.Flowable;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author haixiao.zhang
 * @date 2021/6/10
 * 废弃代码-xml中已注释
 */
@Extension(bizId = ScenarioConstant.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstant.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_SHANG_XIN_CONTENT)
@Service
public class SxlContentInfoQueryExtPt implements ContentInfoQueryExtPt {

    @Override
    public Flowable<Response<Map<Long, ContentInfoDTO>>> process(SgFrameworkContextContent sgFrameworkContextContent) {

        Map<Long, ContentInfoDTO> resMap = Maps.newHashMap();
        Map<String, Object> contentInfo = Maps.newHashMap();
        contentInfo.put("url","111");
        ContentInfoDTO contentInfoDTO = new ContentInfoDTO();
        contentInfoDTO.setContentInfo(contentInfo);
        resMap.put(5233L,contentInfoDTO);
        resMap.put(322385L,contentInfoDTO);
        return Flowable.just(Response.success(resMap));

    }
}
