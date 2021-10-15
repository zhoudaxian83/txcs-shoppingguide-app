package com.tmall.wireless.tac.biz.processor.detail.util;

import java.util.Objects;

/**
 * @author: guichen
 * @Data: 2021/9/10
 * @Description:
 */
public class CommonUtil {
    public static boolean validId(Long id) {
        return Objects.nonNull(id) && id > 0;
    }

    public static boolean validId(Integer id) {
        return Objects.nonNull(id) && id > 0;
    }
}
