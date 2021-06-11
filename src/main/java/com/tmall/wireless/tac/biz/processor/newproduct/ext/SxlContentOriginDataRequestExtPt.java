package com.tmall.wireless.tac.biz.processor.newproduct.ext;

import com.alibaba.aladdin.lamp.domain.request.Request;
import com.alibaba.aladdin.lamp.domain.request.RequestItem;
import com.alibaba.aladdin.lamp.domain.request.modules.DeviceInfo;
import com.alibaba.aladdin.lamp.domain.request.modules.LocationInfo;
import com.alibaba.aladdin.lamp.domain.response.ResResponse;
import com.alibaba.aladdin.lamp.domain.user.UserProfile;
import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ContentOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextContent;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.model.biz.context.LocParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.txcs.gs.spi.recommend.AldSpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.newproduct.constant.Constant;
import com.tmall.wireless.tac.biz.processor.newproduct.handler.SxlItemFeedsHandler;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * tpp入参组装扩展点
 * @author haixiao.zhang
 * @date 2021/6/8
 */
@Extension(bizId = ScenarioConstant.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstant.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_SHANG_XIN_CONTENT)
@Service
public class SxlContentOriginDataRequestExtPt implements ContentOriginDataRequestExtPt {

    Logger LOGGER = LoggerFactory.getLogger(SxlContentOriginDataRequestExtPt.class);

    private static final Long APPID = 25831L;

    @Autowired
    private AldSpi aldSpi;

    @Autowired
    TacLogger tacLogger;

    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {

        getAldInfo(sgFrameworkContextContent);
        tacLogger.info("SxlContentOriginDataRequestExtPt aldResponse:{}"+JSON.toJSONString(sgFrameworkContextContent.getUserParams()));

        /**
         * https://tui.taobao.com/recommend?appid=25831&itemSets=crm_5233&commerce=B2C&regionCode=108&smAreaId=330110&itemSetFilterTriggers=crm_5233&OPEN_MAINTENANCE=1
         */

        /*t
        https://tuipre.taobao.com/recommend?appid=25831&itemSets=crm_296517,crm_296516&commerce=B2C&regionCode=108&smAreaId=330110&itemSetFilterTriggers=crm_296517,crm_296516
        */

        RecommendRequest tppRequest = new RecommendRequest();
        tppRequest.setAppId(APPID);
        Map<String, String> params = Maps.newHashMap();

        Map<String,Object> itemSetMap = (Map<String,Object>)sgFrameworkContextContent.getUserParams().get(Constant.SXL_ITEMSET_PRE_KEY);
        if(MapUtils.isEmpty(itemSetMap)){
            return tppRequest;
        }
        tppRequest.setAppId(APPID);
        params.put("itemSets",  String.join(",", itemSetMap.keySet()));
        params.put("commerce", "B2C");
        params.put("regionCode", String.valueOf(sgFrameworkContextContent.getLocParams().getRegionCode()));
        params.put("smAreaId", Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getLocParams).map(LocParams::getSmAreaId).orElse(0L).toString());
        tppRequest.setUserId(Optional.ofNullable(sgFrameworkContextContent).map(SgFrameworkContext::getUserDO)
            .map(UserDO::getUserId).orElse(0L));
        tppRequest.setParams(params);
        return tppRequest;
    }


    private void getAldInfo(SgFrameworkContextContent sgFrameworkContextContent){

        int index = sgFrameworkContextContent.getUserPageInfo().getIndex();
        int pageSize = sgFrameworkContextContent.getUserPageInfo().getPageSize();
        Map<String, ResResponse> mapResponse = aldSpi.queryAldInfoSync(buildAldRequest(sgFrameworkContextContent));
        Map<String,Object> itemSetMap = Maps.newHashMap();
        sgFrameworkContextContent.getUserParams().put(Constant.SXL_ITEMSET_PRE_KEY,itemSetMap);
        if(MapUtils.isNotEmpty(mapResponse)){
            List<Map<String, Object>> dataList = (List<Map<String, Object>>)mapResponse.get(Constant.CONTENT_ALD_RES_ID).get("data");

            dataList.forEach(e->{
                Integer position = (Integer)e.get("position");
                tacLogger.info("SxlContentOriginDataRequestExtPt position:"+position+"index:"+index+"pageSize:"+pageSize);

                if(position!=null && position < index+pageSize && position>index){
                    itemSetMap.put("crm_"+e.get("itemSetId"),e);
                }
            });

        }

    }

    private Request buildAldRequest(SgFrameworkContextContent sgFrameworkContextContent){
        Request request = new Request();
        request.setBizId(Constant.ALD_BIZ_ID);
        request.setCallSource(Constant.ALD_CALL_SOURCE);
        request.setDebug(false);
        RequestItem item = new RequestItem();
        item.setResId(Constant.CONTENT_ALD_RES_ID);
        UserProfile userProfile = request.getUserProfile();
        userProfile.setUserId(sgFrameworkContextContent.getUserDO().getUserId());
        DeviceInfo deviceInfo = request.getDeviceInfo();
        //deviceInfo.setTtid(sgFrameworkContextContent.get().);
        request.setRequestItems(Lists.newArrayList(item));
        //地址信息
        LocationInfo locationInfo = request.getLocationInfo();
        //四级地址
        locationInfo.setCityLevel4(String.valueOf(sgFrameworkContextContent.getLocParams().getSmAreaId()));
        List<String> wdkCodes = Lists.newArrayList();
        locationInfo.setWdkCodes(wdkCodes);
        return request;

    }

    public static void main(String args[]){

        Map<String,String> map = Maps.newHashMap();
        map.put("111","111");
        map.put("222","222");


        String str = String.join(",", map.keySet());
        System.out.println(str);
    }
}
