package com.tmall.wireless.tac.biz.processor.firstScreenMind.utils;

import com.google.common.collect.Lists;
import com.taobao.eagleeye.EagleEye;
import com.tmall.txcs.gs.model.biz.context.UserDO;

import java.util.List;
import java.util.Random;

/**
 * Created by yangqing.byq on 2021/5/25.
 */

public class PressureTestUtil {

    static List<Long> USER_ID_LIST = Lists.newArrayList(
            2856722021L,
            3138518691L,
            2582261503L,
            3426713633L,
            4139144878L,
            3358239520L
    );


    public static long pressureTestUserId() {
        return USER_ID_LIST.get(new Random().nextInt(USER_ID_LIST.size()));
    }


    public static boolean isFromTest() {
        String ut = EagleEye.getUserData("t");
        if("1".equals(ut) || "2".equals(ut)) {
            return true;
        }
        return false;
    }
}
