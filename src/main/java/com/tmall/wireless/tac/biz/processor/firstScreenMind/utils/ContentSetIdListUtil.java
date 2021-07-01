package com.tmall.wireless.tac.biz.processor.firstScreenMind.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.wireless.tac.biz.processor.common.RequestKeyConstantApp;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

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
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND));

        return result.stream().filter(contentSetId -> contentSetId > 0).collect(Collectors.toList());
    }

    /**
     * 首页普通场景
     * @param requestParams
     * @return
     */
    public static List<Long> getContentSetIdList(Map<String, Object> requestParams) {

        List<Long> result = Lists.newArrayList();
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RANKING));
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RECIPE));
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_BRAND));
       /* result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, 0L));*/
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_O2O));
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_B2C));

        return result.stream().filter(contentSetId -> contentSetId > 0).collect(Collectors.toList());
    }
    /**
     * 承接页不出菜谱、心智
     * @param requestParams
     * @return
     */
    public static List<Long> getContentSetIdListItemFeeds(Map<String, Object> requestParams) {

        List<Long> result = Lists.newArrayList();
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RANKING));
        /*result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RECIPE, 0L));*/
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_BRAND));
       /* result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND, 0L));*/
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_O2O));
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_B2C));

        return result.stream().filter(contentSetId -> contentSetId > 0).collect(Collectors.toList());
    }
    /**
     * 逛超市普通场景
     * @param requestParams
     * @return
     */
    public static List<Long> getGcsContentSetIdList(Map<String, Object> requestParams) {

        List<Long> result = Lists.newArrayList();
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_BRAND));
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_O2O));
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_B2C));
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RECIPE));
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MEDIA));
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RANKING));

        return result.stream().filter(contentSetId -> contentSetId > 0).collect(Collectors.toList());
    }
    /**
     * 逛超市承接页不出菜谱
     * @param requestParams
     * @return
     */
    public static List<Long> getGcsContentSetIdListItem(Map<String, Object> requestParams) {

        List<Long> result = Lists.newArrayList();
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_BRAND));
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_O2O));
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_B2C));
        /*result.add(
            MapUtil.getLongWithDefault(requestParams, RequestKeyConstantApp.VM_CONTENT_SET_RECIPE, 0L));*/
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_MIND));
        result.addAll(
                getLongWithDefault(requestParams, RequestKeyConstantApp.FIRST_SCREEN_SCENE_CONTENT_SET_RANKING));

        return result.stream().filter(contentSetId -> contentSetId > 0).collect(Collectors.toList());
    }

    public static List<Long> getLongWithDefault(Map<String, Object> map, String key) {
        String longListStr = MapUtil.getStringWithDefault(map, key, "");

        if (StringUtils.isEmpty(longListStr)) {
            return Lists.newArrayList();
        }

        List<String> longList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(longListStr);

        return longList.stream().filter(StringUtils::isNumeric).map(Long::valueOf).collect(Collectors.toList());
    }
}
