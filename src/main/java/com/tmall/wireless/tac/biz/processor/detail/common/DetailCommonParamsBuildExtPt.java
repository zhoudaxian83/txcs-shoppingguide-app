package com.tmall.wireless.tac.biz.processor.detail.common;

import java.util.Objects;

import javax.annotation.Resource;

import com.tmall.aselfcommon.lbs.service.LocationReadService.AddressTairDTO;
import com.tmall.tcls.gs.sdk.biz.uti.UserCommonParamsUtil;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.contextbuild.ItemUserCommonParamsBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.constant.RequestKeyConstant;
import com.tmall.tcls.gs.sdk.framework.model.context.CommonUserParams;
import com.tmall.tcls.gs.sdk.framework.model.context.LocParams;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.third.PlaceSpi;
import com.tmall.wireless.tac.biz.processor.detail.common.config.DetailSwitch;
import com.tmall.wireless.tac.client.domain.Context;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: guichen
 * @Data: 2021/9/14
 * @Description:详情没有csa的话，从tair取数据
 */
@Slf4j
public class DetailCommonParamsBuildExtPt extends Register{
    @Resource
    PlaceSpi placeSpi;

    public CommonUserParams process(Context context) {

        CommonUserParams process = UserCommonParamsUtil.process(context);

        if(DetailSwitch.enablePlaceTair){
            LocParams fromAddressTair = getFromAddressTair(process.getUserDO().getUserId());
            if(Objects.nonNull(fromAddressTair)){
                process.setLocParams(fromAddressTair);
            }
        }

        return process;
    }

    private LocParams getFromAddressTair(Long userId) {

        try {
            SPIResult<AddressTairDTO> result = placeSpi.readTairAddressDTO(userId, "CN");
            if (result != null && result.isSuccess() && Objects.nonNull(result.getData())) {
                AddressTairDTO addressDTO = result.getData();
                LocParams locParams = new LocParams();
                locParams.setRt1HourStoreId(
                    addressDTO.getRt1HourStoreId() == null ? 0L : addressDTO.getRt1HourStoreId());
                locParams.setRtHalfDayStoreId(
                    addressDTO.getRtHalfDayStoreId() == null ? 0L : addressDTO.getRtHalfDayStoreId());
                locParams.setSmAreaId(addressDTO.getDivisionId());
                locParams.setRegionCode(
                    StringUtils.isNumeric(addressDTO.getRegionCode()) ? Long.parseLong(addressDTO.getRegionCode())
                        : 0L);
                locParams.setMajorCityCode(
                    StringUtils.isNumeric(addressDTO.getMajorCityCode()) ? Long.parseLong(addressDTO.getMajorCityCode())
                        : 0L);
                return locParams;

            }
        } catch (Exception e) {
            log.error("getFromAddressTair error,userId:{}", userId, e);
        }
        return null;
    }
}
