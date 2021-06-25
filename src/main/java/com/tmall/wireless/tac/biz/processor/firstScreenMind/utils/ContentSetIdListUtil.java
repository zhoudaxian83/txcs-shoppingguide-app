package com.tmall.wireless.tac.biz.processor.firstScreenMind.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.google.common.collect.Lists;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;

/**
 * @author guijian
 */
public class ContentSetIdListUtil {
    /**
     * 心智场景
     * @param requestParams
     * @return
     */
    public static List<Long> getMindContentSetIdList(Map<String, Object> requestParams) {

        List<Long> result = Lists.newArrayList();
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, 0L));

        return result.stream().filter(contentSetId -> contentSetId > 0).collect(Collectors.toList());
    }

    /**
     * 首页普通场景
     * @param requestParams
     * @return
     */
    public static List<Long> getContentSetIdList(Map<String, Object> requestParams) {

        List<Long> result = Lists.newArrayList();
        result.add(MapUtil
            .getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RANKING, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RECIPE, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_BRAND, 0L));
       /* result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, 0L));*/
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_O2O, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_B2C, 0L));

        return result.stream().filter(contentSetId -> contentSetId > 0).collect(Collectors.toList());
    }
    /**
     * 承接页不出菜谱、心智
     * @param requestParams
     * @return
     */
    public static List<Long> getContentSetIdListItemFeeds(Map<String, Object> requestParams) {

        List<Long> result = Lists.newArrayList();
        result.add(MapUtil
            .getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RANKING, 0L));
        /*result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RECIPE, 0L));*/
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_BRAND, 0L));
       /* result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, 0L));*/
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_O2O, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_B2C, 0L));

        return result.stream().filter(contentSetId -> contentSetId > 0).collect(Collectors.toList());
    }
    /**
     * 逛超市普通场景
     * @param requestParams
     * @return
     */
    public static List<Long> getGcsContentSetIdList(Map<String, Object> requestParams) {

        List<Long> result = Lists.newArrayList();
        result.add(MapUtil
            .getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_BRAND, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_O2O, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_B2C, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RECIPE, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MEDIA, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RANKING, 0L));

        return result.stream().filter(contentSetId -> contentSetId > 0).collect(Collectors.toList());
    }
    /**
     * 逛超市承接页不出菜谱
     * @param requestParams
     * @return
     */
    public static List<Long> getGcsContentSetIdListItem(Map<String, Object> requestParams) {

        List<Long> result = Lists.newArrayList();
        result.add(MapUtil
            .getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_BRAND, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_O2O, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_B2C, 0L));
        /*result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.VM_CONTENT_SET_RECIPE, 0L));*/
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, 0L));
        result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RANKING, 0L));

        return result.stream().filter(contentSetId -> contentSetId > 0).collect(Collectors.toList());
    }
}
