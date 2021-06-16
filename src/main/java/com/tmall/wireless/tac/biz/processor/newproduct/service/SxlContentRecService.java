package com.tmall.wireless.tac.biz.processor.newproduct.service;

import com.google.common.collect.Lists;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.txcs.gs.framework.model.meta.*;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceContent;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.newproduct.constant.Constant;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 上新了内容推荐
 * @author haixiao.zhang
 * @date 2021/6/8
 */
@Service
public class SxlContentRecService {

    Logger LOGGER = LoggerFactory.getLogger(SxlContentRecService.class);

    @Autowired
    SgFrameworkServiceContent sgFrameworkServiceContent;

    @Autowired
    TacLogger tacLogger;

    static List<Pair<String, String>> dataTubeKeyList = Lists.newArrayList(
        Pair.of("newItemAttribute","newItemAttribute")
    );

    public Flowable<TacResult<SgFrameworkResponse<ContentVO>>> recommend(Context context) {

        long startTime = System.currentTimeMillis();

        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L);

        SgFrameworkContextContent sgFrameworkContextContent = new SgFrameworkContextContent();
        sgFrameworkContextContent.setRequestParams(context.getParams());

        sgFrameworkContextContent.setSceneInfo(getSceneInfo());
        sgFrameworkContextContent.setUserDO(getUserDO(context));
        sgFrameworkContextContent.setLocParams(CsaUtil
            .parseCsaObj(context.get(UserParamsKeyConstant.USER_PARAMS_KEY_CSA), smAreaId));

        sgFrameworkContextContent.setContentMetaInfo(getContentMetaInfo());

        PageInfoDO pageInfoDO = new PageInfoDO();
        String index = MapUtil.getStringWithDefault(context.getParams(), RequestKeyConstantApp.INDEX, "0");
        String pageSize = MapUtil.getStringWithDefault(context.getParams(), RequestKeyConstantApp.PAGE_SIZE, "10");
        pageInfoDO.setIndex(Integer.valueOf(index));
        pageInfoDO.setPageSize(Integer.valueOf(pageSize));
        sgFrameworkContextContent.setUserPageInfo(pageInfoDO);


        return sgFrameworkServiceContent.recommend(sgFrameworkContextContent)
            .map(response->{
                Map<String,Object> aldMap = (Map<String,Object>)sgFrameworkContextContent.getUserParams().get(Constant.SXL_ITEMSET_PRE_KEY);
                response.getItemAndContentList().forEach(e->{
                    Map<String,Object> objectMap = (Map<String,Object>)aldMap.get("crm_"+e.get("contentId"));
                    objectMap.keySet().forEach(ob->{
                        e.put(ob,objectMap.get(ob));
                    });
                });
                return response;
            })
            .map(TacResult::newResult)
            .onErrorReturn(r -> TacResult.errorResult(""));
    }


    public SceneInfo getSceneInfo(){
        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setBiz(ScenarioConstantApp.BIZ_TYPE_SUPERMARKET);
        sceneInfo.setSubBiz(ScenarioConstantApp.LOC_TYPE_B2C);
        sceneInfo.setScene(ScenarioConstantApp.SCENARIO_SHANG_XIN_CONTENT);
        return sceneInfo;
    }

    public UserDO getUserDO(Context context){
        UserDO userDO = new UserDO();
        userDO.setUserId(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getUserId).orElse(0L));
        userDO.setNick(Optional.of(context).map(Context::getUserInfo).map(UserInfo::getNick).orElse(""));
        if (MapUtils.isNotEmpty(context.getParams())) {
            Object cookies = context.getParams().get("cookies");
            if (cookies != null && cookies instanceof Map) {
                String cna = (String)((Map)cookies).get("cna");
                userDO.setCna(cna);
            }
        }
        return userDO;
    }

    public ContentMetaInfo getContentMetaInfo() {
        ContentMetaInfo contentMetaInfo = new ContentMetaInfo();
        List<ItemInfoSourceMetaInfo> itemInfoSourceMetaInfoList = Lists.newArrayList();
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoTpp = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoTpp.setSourceName("tpp");
        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoTpp);
        ItemInfoSourceMetaInfo itemInfoSourceMetaInfoCaptain = new ItemInfoSourceMetaInfo();
        itemInfoSourceMetaInfoCaptain.setSourceName("captain");
        //captain SceneCode场景code
        itemInfoSourceMetaInfoCaptain.setSceneCode("shoppingguide.newLauch.common");
        itemInfoSourceMetaInfoCaptain.setDataTubeMateInfo(buildDataTubeMateInfo(322385L));

        itemInfoSourceMetaInfoList.add(itemInfoSourceMetaInfoCaptain);

        ItemGroupMetaInfo itemGroupMetaInfo = new ItemGroupMetaInfo();
        itemGroupMetaInfo.setGroupName("sm_B2C");
        itemGroupMetaInfo.setItemInfoSourceMetaInfos(itemInfoSourceMetaInfoList);

        List<ItemGroupMetaInfo> itemGroupMetaInfoList = Lists.newArrayList();
        itemGroupMetaInfoList.add(itemGroupMetaInfo);

        ItemMetaInfo itemMetaInfo = new ItemMetaInfo();
        itemMetaInfo.setItemGroupRenderInfoList(itemGroupMetaInfoList);

        contentMetaInfo.setItemMetaInfo(itemMetaInfo);
        ContentRecommendMetaInfo contentRecommendMetaInfo = new ContentRecommendMetaInfo();
        contentRecommendMetaInfo.setUseRecommendSpiV2(true);
        contentMetaInfo.setContentRecommendMetaInfo(contentRecommendMetaInfo);
        return contentMetaInfo;
    }

    private static DataTubeMateInfo buildDataTubeMateInfo(Long itemSetId) {


        DataTubeMateInfo dataTubeMateInfo = new DataTubeMateInfo();
        dataTubeMateInfo.setActivityId(String.valueOf(itemSetId));
        dataTubeMateInfo.setChannelName("itemExtLdb");
        dataTubeMateInfo.setDataKeyList(dataTubeKeyList.stream().map(k -> {
            DataTubeKey dataTubeKey = new DataTubeKey();
            dataTubeKey.setDataKey(k.getRight());
            dataTubeKey.setVoKey(k.getLeft());
            return dataTubeKey;
        }).collect(Collectors.toList()));
        return dataTubeMateInfo;
    }
}
