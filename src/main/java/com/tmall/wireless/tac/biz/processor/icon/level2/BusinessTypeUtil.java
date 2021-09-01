package com.tmall.wireless.tac.biz.processor.icon.level2;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.tmall.tcls.gs.sdk.framework.model.context.LocParams;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Created by yangqing.byq on 2020/2/7.
 */
public class BusinessTypeUtil {

    public static final String B2C = "B2C";
    public static final String OneHour = "OneHour";
    public static final String HalfDay = "HalfDay";
    public static final String NextDay = "NextDay";

    static Logger LOGGER = LoggerFactory.getLogger(BusinessTypeUtil.class);


    public static String processType(LocParams locParams, String o2oBizType) {
        try {
            if (StringUtils.isEmpty(o2oBizType) || locParams == null) {
                return B2C;
            }

            Set<String> userCurrentBiz = Sets.newHashSet(B2C);

            if (locParams.getRt1HourStoreId() > 0) {
                userCurrentBiz.add(OneHour);
            } else if (locParams.getRtHalfDayStoreId() > 0) {
                userCurrentBiz.add(HalfDay);
            }
            //支持次日达业务类型
            if (locParams.getRtNextDayStoreId() > 0) {
                userCurrentBiz.add(NextDay);
            }

            Set<String> bizResultList = Sets.newHashSet();

            String[] split = o2oBizType.split("_");

            for (String s : split) {
                if (userCurrentBiz.contains(s)) {
                    bizResultList.add(s);
                }
            }

            return Joiner.on(",").join(bizResultList);
        } catch (Exception e) {
            LOGGER.error("getBusinessType error:{},{}", o2oBizType, JSON.toJSONString(locParams));
        }

        return B2C;
    }
}
