package com.tmall.wireless.tac.biz.processor.cnxh;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.cola.extension.Extension;
import com.alibaba.fastjson.JSON;

import com.tmall.aself.shoppingguide.client.loc.util.AddressUtil;
import com.tmall.aselfcommon.model.todaycrazy.enums.LogicalArea;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.excutor.SgExtensionExecutor;
import com.tmall.txcs.gs.framework.extensions.origindata.request.ItemOriginDataRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.spi.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.cnxh.enums.O2OChannelEnum;
import com.tmall.wireless.tac.biz.processor.cnxh.utils.O2OChannelUtil;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author luojunchong
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.O2O_CNXH)
@Service
public class CaiNiXiHuanItemOriginDataRequestExtPt implements ItemOriginDataRequestExtPt {
    @Autowired
    TacLogger tacLogger;

    @Override
    public RecommendRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        return this.buildTppParams(sgFrameworkContextItem);
    }

    private RecommendRequest buildTppParams(SgFrameworkContextItem context) {
        tacLogger.info("扩展点数据：" + JSON.toJSONString(context));
        String pageId = "pageId";
        String itemBusinessType = "itemBusinessType";
        RecommendRequest recommendRequest = new RecommendRequest();
        Map<String, String> params = new HashMap<>(16);
        String csa = MapUtil.getStringWithDefault(context.getRequestParams(), "csa", "");
        String O2OChannel = MapUtil.getStringWithDefault(context.getRequestParams(), "O2OChannel", "");
        O2OChannel = O2OChannel.equals("") ? O2OChannelUtil.getO2OChannel(csa) : O2OChannel;
        String moduleId = MapUtil.getStringWithDefault(context.getRequestParams(), "moduleId", "");
        Long appId = this.getAppId(O2OChannel);
        Long index = MapUtil.getLongWithDefault(context.getRequestParams(), "index", 0L);
        Long userId = MapUtil.getLongWithDefault(context.getRequestParams(), "userId", 0L);
        Long pageSize = MapUtil.getLongWithDefault(context.getRequestParams(), "pageSize", 20L);
        Long itemSetId = MapUtil.getLongWithDefault(context.getRequestParams(), "itemSetId", 0L);
        Long smAreaId = context.getLocParams().getSmAreaId() == 0 ? LogicalArea.parseByCode(
            AddressUtil.parseCSA(csa).getRegionCode()).getCoreCityCode() : context.getLocParams().getSmAreaId();
        String logicAreaId = AddressUtil.parseCSA(csa).getRegionCode();
        params.put("itemSetIdSource", "crm");
        params.put("pmtSource", "sm_manager");
        params.put("pmtName", "o2oGuessULike");
        params.put("smAreaId", smAreaId + "");
        params.put("userId", String.valueOf(userId));
        params.put("index", index + "");
        params.put("pageSize", pageSize + "");
        params.put(pageId, appId + "");
        params.put("logicAreaId", logicAreaId);
        params.put("itemSetIdList", itemSetId + "");
        params.put("isFirstPage", (index == 0L) + "");
        if (O2OChannelEnum.ONE_HOUR.getCode().equals(O2OChannel)) {
            params.put(pageId, "onehourcnxh");
            params.put(itemBusinessType, "OneHour");
            params.put("rt1HourStoreId", String.valueOf(context.getLocParams().getRt1HourStoreId()));
            params.put("itemBusinessType", "OneHour");
            params.put("moduleId", moduleId);
        } else if (O2OChannelEnum.HALF_DAY.getCode().equals(O2OChannel)) {
            params.put(pageId, "halfdaycnxh");
            params.put(itemBusinessType, "HalfDay");
            params.put("rtHalfDayStoreId", String.valueOf(context.getLocParams().getRtHalfDayStoreId()));
            params.put("moduleId", moduleId);

        } else if (O2OChannelEnum.NEXT_DAY.getCode().equals(O2OChannel)) {
            params.put(pageId, "nextdaycnxh");
            params.put(itemBusinessType, "NextDay");
        } else if (O2OChannelEnum.ALL_FRESH.getCode().equals(O2OChannel)) {
            params.put("pageId", "onehourcnxh");
            params.put(itemBusinessType, "B2C");
        }

        recommendRequest.setAppId(appId);
        recommendRequest.setLogResult(true);
        recommendRequest.setParams(params);
        recommendRequest.setUserId(userId);
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
