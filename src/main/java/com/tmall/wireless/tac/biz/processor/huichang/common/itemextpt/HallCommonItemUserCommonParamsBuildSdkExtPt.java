package com.tmall.wireless.tac.biz.processor.huichang.common.itemextpt;

import java.util.List;
import java.util.Map;

import com.alibaba.aladdin.lamp.domain.request.Request;
import com.alibaba.aladdin.lamp.domain.request.RequestItem;
import com.alibaba.aladdin.lamp.domain.request.modules.LocationInfo;
import com.alibaba.aladdin.lamp.domain.response.ResResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Lists;
import com.tmall.aself.shoppingguide.client.loc.domain.AddressDTO;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.tcls.gs.sdk.biz.uti.MapUtil;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkPackage;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextbuild.ItemUserCommonParamsBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.CommonUserParams;
import com.tmall.tcls.gs.sdk.framework.model.context.LocParams;
import com.tmall.tcls.gs.sdk.framework.model.context.PageInfoDO;
import com.tmall.tcls.gs.sdk.framework.model.context.UserDO;
import com.tmall.txcs.gs.spi.recommend.AldSpi;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallCommonAldConstant;
import com.tmall.wireless.tac.biz.processor.huichang.common.constant.HallScenarioConstant;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

//@Service
@SdkPackage(packageName = HallScenarioConstant.HALL_ITEM_SDK_PACKAGE)
public class HallCommonItemUserCommonParamsBuildSdkExtPt extends Register implements ItemUserCommonParamsBuildSdkExtPt {


    Logger logger = LoggerFactory.getLogger(HallCommonItemUserCommonParamsBuildSdkExtPt.class);



    public final static  String EPIDEMIC_STOCK_RESOURCE = "24665734";

    public static final String yxsdPrefix = "SG_TMCS_1H_DS:";

    public static final String brdPrefix = "SG_TMCS_HALF_DAY_DS:";

    private static final String APP_NAME = "txcs-shoppingguide-app";

    @Autowired
    private AldSpi aldSpi;

    @Override
    public CommonUserParams process(Context context) {
        logger.info("-------HallCommonItemUserCommonParamsBuildSdkExtPt.start------------");

        RequestContext4Ald requestContext4Ald = (RequestContext4Ald)context;
        Map<String, Object> aldParam = requestContext4Ald.getAldParam();//对应requestItem
        Map<String, Object> aldContext = requestContext4Ald.getAldContext();//对应solutionContext
        CommonUserParams commonUserParams = new CommonUserParams();
        UserDO userDO = new UserDO();
        Long userId = MapUtil.getLongWithDefault(aldContext, HallCommonAldConstant.UTDID, 0L);
        String userNick = MapUtil.getStringWithDefault(aldContext, HallCommonAldConstant.USER_NICK, "");
        userDO.setUserId(userId);
        userDO.setNick(userNick);
        commonUserParams.setUserDO(userDO);

        Long smAreaId = MapUtil.getLongWithDefault(aldParam, HallCommonAldConstant.SM_AREAID, 330100L);
        commonUserParams.setLocParams(parseCsaObj(aldParam.get(HallCommonAldConstant.CSA), smAreaId));

        Integer pageSize = MapUtil.getIntWithDefault(aldContext, HallCommonAldConstant.PAGE_SIZE, 20);
        Integer pageIndex = MapUtil.getIntWithDefault(aldContext, HallCommonAldConstant.PAGE_INDEX, 0);

        PageInfoDO pageInfoDO = new PageInfoDO();
        pageInfoDO.setPageSize(pageSize);
        pageInfoDO.setIndex(pageIndex);
        commonUserParams.setUserPageInfo(pageInfoDO);

        return commonUserParams;
    }

    public static LocParams parseCsaObj(Object csa, Long smAreaId) {
        if (csa != null && !StringUtils.isEmpty(csa.toString())) {
            return parseCsa(csa.toString(), smAreaId);
        } else {
            LocParams locParams = new LocParams();
            locParams.setSmAreaId(smAreaId);
            return locParams;
        }
    }



    public static LocParams parseCsa(String csa, Long smAreaId) {
        AddressDTO addressDTO = AddressUtil.parseCSA(csa);
        LocParams locParams;
        if (addressDTO == null) {
            locParams = new LocParams();
            locParams.setSmAreaId(smAreaId);
            return locParams;
        } else {
            locParams = new LocParams();
            locParams.setRt1HourStoreId(addressDTO.getRt1HourStoreId());
            locParams.setRtHalfDayStoreId(addressDTO.getRtHalfDayStoreId());
            locParams.setSmAreaId(smAreaId);
            locParams.setRegionCode(StringUtils.isNumeric(addressDTO.getRegionCode()) ? Long.parseLong(addressDTO.getRegionCode()) : 0L);
            locParams.setMajorCityCode(StringUtils.isNumeric(addressDTO.getMajorCityCode()) ? Long.parseLong(addressDTO.getMajorCityCode()) : 0L);
            return locParams;
        }
    }



    private void setEpidemicStockClose(CommonUserParams commonUserParams) {
        boolean isEpidemicStockClose = false;
        try {
            long userId = commonUserParams.getUserDO().getUserId() == null ? 0 : commonUserParams.getUserDO().getUserId();
            long smAreaId = commonUserParams.getLocParams().getSmAreaId();
            isEpidemicStockClose =  getAldResourceResponse(userId, commonUserParams.getLocParams());
        } catch (Exception e) {
            // todo log
        }
        commonUserParams.setEpidemicStockClose(isEpidemicStockClose);
    }

    private boolean getAldResourceResponse(Long userId, LocParams locParams) {
        //从阿拉丁拿到
        Map<String, ResResponse> aldResponseMap = aldSpi.queryAldInfoSync(buildAldRequest(userId, locParams));
        if(MapUtils.isEmpty(aldResponseMap)){
            return false;
        }
        ResResponse resResponse = aldResponseMap.get(EPIDEMIC_STOCK_RESOURCE);
        Object data = resResponse.getData();
        if (data != null) {

            JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(data));
            if (jsonArray.size() == 0) {
                return Boolean.FALSE;
            }
            JSONObject jsonObject = (JSONObject)jsonArray.get(0);
            String epidemicStock = jsonObject.getString("epidemicStock");
            if (StringUtils.equalsIgnoreCase(epidemicStock, "true")) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }


    private Request buildAldRequest(Long userId, LocParams locParams) {
        Request request = new Request();

        request.setCallSource(APP_NAME);
        RequestItem item = new RequestItem();
        item.setCount(50);
        item.setResId(EPIDEMIC_STOCK_RESOURCE);
        JSONObject data = new JSONObject();

        //地址信息
        LocationInfo locationInfo = request.getLocationInfo();
        //四级地址
        List<String> wdkCodes = Lists.newArrayList();
        if (locParams.getRt1HourStoreId() != 0L) {
            wdkCodes.add(yxsdPrefix + locParams.getRt1HourStoreId());
        } else if (locParams.getRtHalfDayStoreId() != 0L) {
            wdkCodes.add(brdPrefix + locParams.getRtHalfDayStoreId());
        }
        data.put("smAreaId", locParams.getSmAreaId());
        locationInfo.setWdkCodes(wdkCodes);

        item.setData(data);
        request.setRequestItems(Lists.newArrayList(item));
        request.getUserProfile().setUserId(userId);

        return request;
    }

}
