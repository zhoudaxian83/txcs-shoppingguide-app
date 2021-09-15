package com.tmall.wireless.tac.biz.processor.detail.common.extabstract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.model.context.CommonUserParams;
import com.tmall.tcls.gs.sdk.framework.model.context.LocParams;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContext;
import com.tmall.tcls.gs.sdk.framework.model.context.UserDO;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.TppItemBusinessType;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.TppParmasConstant;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.util.CommonUtil;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.LocTypeEnum;
import org.apache.commons.lang.StringUtils;

/**
 * @author: guichen
 * @Data: 2021/9/10
 * @Description:
 */
public abstract class AbstractDetailOriginDataRequestBuildSdkExtPt extends Register {

    protected abstract Long getAppId(String recType,SgFrameworkContext sgFrameworkContextContent);

    public RecommendRequest processRequest(SgFrameworkContext sgFrameworkContextContent) {

        //构建tpp参数
        DetailRecommendRequest detailRequest = DetailRecommendRequest.getDetailRequest(
            sgFrameworkContextContent.getTacContext());


        RecommendRequest recommendRequest=new RecommendRequest();
        recommendRequest.setAppId(getAppId(detailRequest.getRecType(),sgFrameworkContextContent));
        recommendRequest.setUserId(Optional.of(sgFrameworkContextContent).
            map(SgFrameworkContext::getCommonUserParams).
            map(CommonUserParams::getUserDO).map(UserDO::getUserId).orElse(0L));

        Map<String, String> tppParams = new HashMap<>();
        recommendRequest.setParams(tppParams);

        tppParams.put("appid",String.valueOf(recommendRequest.getAppId()));
        tppParams.put("userId",String.valueOf(recommendRequest.getUserId()));

        //1.商品
        tppParams.put(TppParmasConstant.ITEM_IDS, String.valueOf(detailRequest.getDetailItemId()));

        //2.分页
        tppParams.put(TppParmasConstant.PAGE_SIZE,String.valueOf(Optional.ofNullable(detailRequest.getPageSize())
            .orElse(20)));
        tppParams.put(TppParmasConstant.IS_FIRST_PAGE,String.valueOf(1 <=detailRequest.getIndex()));

        //3.店铺等信息
        buildTppRequest(tppParams,detailRequest.getLocType(),sgFrameworkContextContent.getCommonUserParams());

        return recommendRequest;
    }

    private void buildTppRequest(Map<String, String> tppParams, String locType,
        CommonUserParams commonUserParams) {

        LocParams address = commonUserParams.getLocParams();
        if (Objects.isNull(address)) {
            //默认107大区打底
            tppParams.put(TppParmasConstant.LOGIC_AREA_ID, "107");
            return;
        }

        //如果获得address就取address的数据
        if (CommonUtil.validId(address.getRegionCode())) {
            tppParams.put(TppParmasConstant.SM_AREA_ID, String.valueOf(address.getSmAreaId()));
            tppParams.put(TppParmasConstant.LOGIC_AREA_ID, String.valueOf(address.getRegionCode()));
        }

        List<String> itemBusinessType = new ArrayList<>();

        if (StringUtils.equals(locType, LocTypeEnum.O2OOneHour.getType()) && CommonUtil.validId(address.getRt1HourStoreId())) {
            tppParams.put(TppParmasConstant.RT_ONE_HOUR_STORE_ID, String.valueOf(address.getRt1HourStoreId()));
            itemBusinessType.add(TppItemBusinessType.OneHour.name());

        } else if (StringUtils.equals(locType, LocTypeEnum.O2OHalfDay.getType()) && CommonUtil.validId(
            address.getRtHalfDayStoreId())) {
            tppParams.put(TppParmasConstant.RT_HALF_DAY_STORE_ID,
                String.valueOf(address.getRtHalfDayStoreId()));
            itemBusinessType.add(TppItemBusinessType.HalfDay.name());

        } else if ((StringUtils.equals(locType, LocTypeEnum.O2ONextDay.getType())) && CommonUtil.validId(
            address.getRtNextDayStoreId())) {
            tppParams.put(TppParmasConstant.RT_NEXT_DAY_STORE_ID,
                String.valueOf(address.getRtNextDayStoreId()));
            itemBusinessType.add(TppItemBusinessType.NextDay.name());

        } else {
            itemBusinessType.add(TppItemBusinessType.B2C.name());
            locType = LocTypeEnum.B2C.getType();
        }

        tppParams.put(TppParmasConstant.STRATEGY_2_IRECAL_KEY, locType);
        tppParams.put(TppParmasConstant.ITEM_BUSINESS_TYPE, String.join(",", itemBusinessType));

    }
}
