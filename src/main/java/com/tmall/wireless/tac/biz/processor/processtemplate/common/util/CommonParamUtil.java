package com.tmall.wireless.tac.biz.processor.processtemplate.common.util;

import org.apache.commons.lang3.StringUtils;

public final class CommonParamUtil {

    public static final String DEFAULT_SM_AREA_ID = "330100";
    public static final String DEFAULT_LOGIC_AREA_ID = "107";

    /**
     * 是否是有效的smAreaId, 如果smAreaId为空或者为"0"，代表非法的smAreaId，此时对于必传smAreaId的场景，需要设置默认值"330100"
     * @param smAreaId
     * @return
     */
    public static boolean isValidSmAreaId(String smAreaId) {
        if(StringUtils.isNotBlank(smAreaId) && !"0".equals(smAreaId)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否是有效的logicAreaId, 如果logicAreaId为空或者为"0"，代表非法的logicAreaId，此时对于必传logicAreaId的场景，需要设置默认值"107"
     * @param logicAreaId
     * @return
     */
    public static boolean isValidLogicAreaId(String logicAreaId) {
        if(StringUtils.isNotBlank(logicAreaId) && !"0".equals(logicAreaId)) {
            return true;
        } else {
            return false;
        }
    }
}
