package com.tmall.wireless.tac.biz.processor.newproduct.handler;

import com.alibaba.aladdin.lamp.domain.request.Request;
import com.alibaba.aladdin.lamp.domain.request.RequestItem;
import com.alibaba.aladdin.lamp.domain.request.modules.DeviceInfo;
import com.alibaba.aladdin.lamp.domain.request.modules.LocationInfo;
import com.alibaba.aladdin.lamp.domain.response.ResResponse;
import com.alibaba.aladdin.lamp.domain.user.UserProfile;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.*;
import com.tmall.txcs.gs.spi.recommend.AldSpi;
import com.tmall.wireless.tac.biz.processor.newproduct.constant.Constant;
import com.tmall.wireless.tac.biz.processor.newproduct.service.SxlItemRecService;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 上新了超市商品推荐
 * @author haixiao.zhang
 * @date 2021/6/7
 */
@Component
public class SxlItemFeedsHandler extends RpmReactiveHandler<SgFrameworkResponse<EntityVO>> {

    private Logger LOGGER = LoggerFactory.getLogger(SxlItemFeedsHandler.class);

    @Autowired
    private SxlItemRecService sxlItemRecService;

    @Autowired
    TacLogger tacLogger;

    @Autowired
    private AldSpi aldSpi;

    @Override
    public Flowable<TacResult<SgFrameworkResponse<EntityVO>>> executeFlowable(Context context) throws Exception {

        LOGGER.error("SxlItemFeedsHandler ITEM_REQUEST:{}", JSON.toJSONString(context));

        HadesLogUtil.debug("SxlItemFeedsHandler ITEM_REQUEST:{}" + JSON.toJSONString(context));

        Flowable<TacResult<SgFrameworkResponse<EntityVO>>> tacResultFlowable = sxlItemRecService.recommend(context);

        /**
         * itemImg":"https://gw.alicdn.com/imgextra/i3/O1CN01Jn1zcv262Dy0MjOHV_!!6000000007603-2-tps-354-414.png","sellPoint":"如果夏天有香气，我猜一定是蜜桃味的～","distinctId":"1623992516548","dataSetId":17951090,"itemTitle":"Skinfood清爽蜜桃味水乳","auctionTag":"https://gw.alicdn.com/imgextra/i4/O1CN01DpBPTa1Tn9XIPZvgj_!!6000000002426-2-tps-400-200.png"
         */
        List<Map<String, Object>> aldResList = getAldInfo(context);

        tacLogger.info("aldResList:"+JSON.toJSONString(aldResList));
        if(CollectionUtils.isEmpty(aldResList)){
            return tacResultFlowable;
        }else{
            /**
             * 白底图 ：itemImg2
             * 标签：auctionTag
             */
            return tacResultFlowable.map(response->{
                List<EntityVO> list = response.getData().getItemAndContentList();
                EntityVO entityVO = new EntityVO();
                entityVO.put("itemId",aldResList.get(0).get("itemId"));
                entityVO.put("itemImg",aldResList.get(0).get("itemImg2"));
                entityVO.put("sellingPointDesc",aldResList.get(0).get("sellPoint"));
                entityVO.put("type",aldResList.get(0).get("auctionTag"));

                list.add(0,entityVO);
                EntityVO entityVO1 = new EntityVO();
                entityVO1.put("itemId",aldResList.get(1).get("itemId"));
                entityVO1.put("itemImg",aldResList.get(1).get("itemImg2"));
                entityVO1.put("sellingPointDesc",aldResList.get(1).get("sellPoint"));
                entityVO1.put("type",aldResList.get(0).get("auctionTag"));
                list.add(entityVO1);
                return response;
            }).onErrorReturn((r -> TacResult.errorResult("")));
        }
    }

    private List<Map<String, Object>> getAldInfo(Context context){

        String source = MapUtil.getStringWithDefault(context.getParams(), "source", "");

        if(StringUtils.isNotBlank(source) && Constant.SXL_SOURCE_AGREE.contains(source)){
            Map<String, ResResponse> mapResponse = aldSpi.queryAldInfoSync(buildAldRequest(context));
            if(MapUtils.isNotEmpty(mapResponse)){
                List<Map<String, Object>> dataList = (List<Map<String, Object>>)mapResponse.get(Constant.ITEM_ALD_RES_ID).get("data");
                return dataList;
            }
        }

        return null;
    }

    private Request buildAldRequest(Context context){
        Request request = new Request();
        request.setBizId(Constant.ALD_BIZ_ID);
        request.setCallSource(Constant.ALD_CALL_SOURCE);
        request.setDebug(false);
        RequestItem item = new RequestItem();
        item.setResId(Constant.ITEM_ALD_RES_ID);
        UserProfile userProfile = request.getUserProfile();
        userProfile.setUserId(context.getUserInfo().getUserId());
        DeviceInfo deviceInfo = request.getDeviceInfo();
        //deviceInfo.setTtid(sgFrameworkContextContent.get().);
        request.setRequestItems(Lists.newArrayList(item));
        //地址信息
        LocationInfo locationInfo = request.getLocationInfo();
        //四级地址
        Long smAreaId = MapUtil.getLongWithDefault(context.getParams(), "smAreaId", 330100L);
        locationInfo.setCityLevel4(String.valueOf(smAreaId));
        List<String> wdkCodes = Lists.newArrayList();
        locationInfo.setWdkCodes(wdkCodes);
        return request;

    }

}
