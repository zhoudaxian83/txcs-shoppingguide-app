package com.tmall.wireless.tac.biz.processor.o2opromise;

import com.tmall.aself.shoppingguide.client.loc.domain.AddressDTO;
import com.tmall.txcs.biz.supermarket.scene.UserParamsKeyConstant;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.base.RpmReactiveHandler;
import com.tmall.txcs.gs.model.spi.model.StoreTimeSliceDTO;
import com.tmall.txcs.gs.model.spi.model.StoreTimeSliceRequest;
import com.tmall.txcs.gs.spi.recommend.PromiseServiceSpi;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderAddressUtil;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacOptLogger;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.UserInfo;
import io.reactivex.Flowable;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author yuexin.tyx
 * @create 2021-07-09 13:00:00
 * mmc半日达门店最优时间片handler
 */
@Service
public class MmcO2OPromiseHandler extends RpmReactiveHandler<Map<String,Object>> {

    public static final String SOURCE = "source";

    @Autowired
    private TacOptLogger tacLogger;

    @Autowired
    private PromiseServiceSpi promiseServiceSpi;

    public static final String WAVE_ARRIVE_TIME_MIND = "WAVE_ARRIVE";

    public static final String WAVE_ARRIVE_TIME_TYPE = "40";

    public static final String WAVE_ARRIVE_QUERY_SCENE = "SearchScene";

    public static final String WAVE_ARRIVE_QUERY_FROM = "MC";

    @Override
    public Flowable<TacResult<Map<String,Object>>> executeFlowable(Context context) throws Exception {
        long startTime = System.currentTimeMillis();
        Long userId = Optional.of(context).map(Context::getUserInfo).map(UserInfo::getUserId).orElse(0L);
        String csa = MapUtil.getStringWithDefault(context.getParams(),UserParamsKeyConstant.USER_PARAMS_KEY_CSA,
                MapUtil.getStringWithDefault(context.getParams(),UserParamsKeyConstant.USER_PARAMS_KEY_CSA,null));
        //获取地址列表
        AddressDTO addressDTO = RenderAddressUtil.getAddressDTO(csa);
        if(null == addressDTO) {
            tacLogger.warn("[MMC_PROMISE] promise store time slice empty, csa is empty");
            return Flowable.just(TacResult.errorResult("EMPTY_CSA"));
        }
        //非半日达门店直接返回
        if(null == addressDTO.getRtHalfDayStoreId()) {
            tacLogger.warn("[MMC_PROMISE] promise store time slice empty, not half_day store");
            return Flowable.just(TacResult.errorResult("EMPTY_HALF_DAY_STORE"));
        }
        StoreTimeSliceRequest request = new StoreTimeSliceRequest();
        request.setQueryFrom(WAVE_ARRIVE_QUERY_FROM);
        request.setChannelStoreId(String.valueOf(addressDTO.getRtHalfDayStoreId()));
        request.setLatitude(addressDTO.getLatitude());
        request.setLongitude(addressDTO.getLongitude());
        request.setQueryScene(WAVE_ARRIVE_QUERY_SCENE);
        request.setTimeMind(WAVE_ARRIVE_TIME_MIND);
        //request.setMerchantCode();
        request.setTimeType(WAVE_ARRIVE_TIME_TYPE);
        request.setUserId(String.valueOf(userId));

        return promiseServiceSpi.calcStoreFirstTimeSliceFlowable(request)
            .map(slice -> {
                if(!slice.isSuccess() || null == slice.getData() || StringUtils.isEmpty(slice.getData().getTimeSlot()) || null == slice.getData().getFirstTimeDate()) {
                    TacResult<Map<String,Object>> result = TacResult.errorResult(slice.getMsgCode(), slice.getMsgInfo());
                    return result;
                }
                StoreTimeSliceDTO storeTimeSliceDTO = slice.getData();
                Map<String, Object> resultData = new HashMap<String, Object>();
                resultData.put("displayTimeStr", storeTimeSliceDTO.getFirstTimeDateOrigin());
                resultData.put("displayTime", displayTimeSlice(storeTimeSliceDTO));
                return TacResult.newResult(resultData);
            }).onErrorReturn(e -> {
                tacLogger.error("[MMC_PROMISE] promise calc store first time slice failed", e);
                return TacResult.errorResult("SYSTEM_ERROR");
            }).defaultIfEmpty(TacResult.errorResult("SYSTEM_ERROR"))
              .doOnTerminate(() -> {
                String utdid = context.getDeviceInfo().getUtdid();
                long sessionTime = System.currentTimeMillis() - startTime;
                tacLogger.info("[MMC_PROMISE] cost={}, utdid={}, userId={}, csa:{}", sessionTime, utdid, userId, csa);
            });
    }

    /**
     * 展示时间片
     * @param storeTimeSliceDTO
     * @return
     */
    private static String displayTimeSlice(StoreTimeSliceDTO storeTimeSliceDTO){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = format.format(new Date());
        Date firstTimeDate = storeTimeSliceDTO.getFirstTimeDate();
        String displayDay = firstTimeDate.equals(todayDate) ? "今天": "明天";
        return StringUtils.join(new String[]{"预计",displayDay, storeTimeSliceDTO.getTimeSlot(), "送货上门"});
    }

}
