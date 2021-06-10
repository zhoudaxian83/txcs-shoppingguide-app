package com.tmall.wireless.tac.biz.processor.newproduct.handler;

import com.alibaba.aladdin.lamp.domain.request.Request;
import com.alibaba.aladdin.lamp.domain.request.RequestItem;
import com.alibaba.aladdin.lamp.domain.request.modules.DeviceInfo;
import com.alibaba.aladdin.lamp.domain.request.modules.LocationInfo;
import com.alibaba.aladdin.lamp.domain.response.ResResponse;
import com.alibaba.aladdin.lamp.domain.user.UserProfile;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.CsaUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.framework.model.*;
import com.tmall.txcs.gs.framework.model.meta.*;
import com.tmall.txcs.gs.framework.service.impl.SgFrameworkServiceItem;
import com.tmall.txcs.gs.model.biz.context.EntitySetParams;
import com.tmall.txcs.gs.model.biz.context.PageInfoDO;
import com.tmall.txcs.gs.model.biz.context.SceneInfo;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.spi.recommend.AldSpi;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model.BannerItemDTO;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model.BannerItemVO;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model.BannerVO;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.uitl.BannerUtil;
import com.tmall.wireless.tac.biz.processor.newproduct.constant.Constant;
import com.tmall.wireless.tac.biz.processor.newproduct.service.SxlContentRecService;
import com.tmall.wireless.tac.biz.processor.newproduct.service.SxlItemRecService;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

        getAldInfo(context);
        tacResultFlowable.map(e->{

            List<EntityVO> lis = e.getData().getItemAndContentList();
            EntityVO entityVO = new EntityVO();
            return e;
        }).onErrorReturn((r -> TacResult.errorResult("")));

        return tacResultFlowable;

    }

    private void getAldInfo(Context context){

        Map<String, ResResponse> mapResponse = aldSpi.queryAldInfoSync(buildAldRequest(context));

        if(MapUtils.isNotEmpty(mapResponse)){
            List<Map<String, Object>> dataList = (List<Map<String, Object>>)mapResponse.get(Constant.ITEM_ALD_RES_ID).get("data");
            tacLogger.info("getAldInfo:"+JSON.toJSONString(dataList));

            dataList.forEach(e->{

            });

        }

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
