package com.tmall.wireless.tac.biz.processor.o2ocn.ext;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

import com.tmall.aself.shoppingguide.client.loc.domain.AddressDTO;
import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.aselfcommon.model.todaycrazy.enums.LogicalArea;
import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.o2ocn.enums.O2OChannelEnum;
import com.tmall.wireless.tac.biz.processor.o2ocn.utils.Constants;
import com.tmall.wireless.tac.biz.processor.o2ocn.utils.O2OChannelUtil;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author luojunchong
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_O2O,
    scenario = ScenarioConstantApp.O2O_CNXH)
@Service
public class CntemOriginDataRequestExtPt implements ItemOriginDataRequestExtPt {

    @Autowired
    TacLogger tacLogger;

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        return this.buildTppParams(sgFrameworkContextItem);
    }

    /**
     * https://tui.taobao.com/recommend?appid=22171&itemSetIdList=13545&smAreaId=330110&rt1HourStoreId=233930038&userId=1832025789&itemBusinessType=OneHour&isFirstPage=true
     * @param context
     * @return
     */
    private RecommendRequest buildTppParams(SgFrameworkContextItem context) {
        RecommendRequest recommendRequest = new RecommendRequest();
        Map<String, String> params = new HashMap<>(16);
        String csa = MapUtil.getStringWithDefault(context.getRequestParams(), "csa", "");
        AddressDTO addressDTO = null;
        if(StringUtils.isNotBlank(csa)){
            addressDTO = AddressUtil.parseCSA(csa);
        }
        recommendRequest.setAppId(Long.valueOf(Constants.APP_ID));
        String O2OChannel = MapUtil.getStringWithDefault(context.getRequestParams(), "O2OChannel", "");
        O2OChannel = O2OChannel.equals("") ? O2OChannelUtil.getO2OChannel(csa) : O2OChannel;
        Long index = MapUtil.getLongWithDefault(context.getRequestParams(), "index", 0L);
        Long userId = MapUtil.getLongWithDefault(context.getRequestParams(), "userId", 0L);
        Long smAreaId = context.getLocParams().getSmAreaId() == 0 ? LogicalArea.parseByCode(
            AddressUtil.parseCSA(csa).getRegionCode()).getCoreCityCode() : context.getLocParams().getSmAreaId();
        String logicAreaId = AddressUtil.parseCSA(csa).getRegionCode();
        params.put("smAreaId", String.valueOf(smAreaId));
        params.put("userId", String.valueOf(userId));
        params.put("appid", Constants.APP_ID);
        params.put("logicAreaId", logicAreaId);
        params.put("isFirstPage", (index == 0L) + "");
        if (O2OChannelEnum.ONE_HOUR.getCode().equals(O2OChannel)
            || (addressDTO!=null && addressDTO.isRt1HourStoreCover())) {
            params.put("rt1HourStoreId", String.valueOf(context.getLocParams().getRt1HourStoreId()));
            params.put("itemBusinessType", "OneHour");
            params.put("itemSetIdList", Constants.O2O_ITEMSET_ID);
        } else if (O2OChannelEnum.HALF_DAY.getCode().equals(O2OChannel)
            || (addressDTO!=null && addressDTO.isRtHalfDayStoreCover())) {
            params.put("itemBusinessType", "HalfDay");
            params.put("rtHalfDayStoreId", String.valueOf(context.getLocParams().getRtHalfDayStoreId()));
            params.put("itemSetIdList", Constants.O2O_ITEMSET_ID);
        }else if(O2OChannelEnum.ALL_FRESH.getCode().equals(O2OChannel) ||
            (addressDTO!=null && !addressDTO.isRtStoreCover())){
            params.put("itemBusinessType", "B2C");
            params.put("itemSetIdList", Constants.ALL_FRESH_ITEMSET_ID);
        }
        recommendRequest.setParams(params);
        recommendRequest.setUserId(userId);
        tacLogger.info("recommendRequest:" + JSON.toJSONString(recommendRequest));

        HadesLogUtil.stream(ScenarioConstantApp.O2O_CNXH)
            .kv("step", "tppRequest")
            .kv("tppRequest",JSON.toJSONString(recommendRequest))
            .error();
        return recommendRequest;

    }

    /**
     * 默认21896L
     *
     * @param code
     * @return
     */
    private Long getAppId(String code) {
        O2OChannelEnum o2OChannelEnum = O2OChannelEnum.ofCode(code);
        if (o2OChannelEnum != null) {
            o2OChannelEnum.getAppId();
        }
        return O2OChannelEnum.ALL_FRESH.getAppId();

    }

}
