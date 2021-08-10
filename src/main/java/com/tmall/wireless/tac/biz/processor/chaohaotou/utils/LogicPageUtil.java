package com.tmall.wireless.tac.biz.processor.chaohaotou.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author luojunchong
 */
public class LogicPageUtil {

    /**
     * 手动分页
     *
     * @param originalList 分页前数据
     * @param index        页码
     * @param pageSize     每页数量
     * @return 分页后结果
     */
    public static <T> Pair<Boolean, List<T>> getPage(List<T> originalList, Long index, Long pageSize) {
        if (index < 1) {
            index = 1L;
        }
        if (pageSize <= 0) {
            pageSize = 20L;
        }
        //第一页，每页数据大于总数据时全部返回
        if (index == 1 && pageSize > originalList.size()) {
            return Pair.of(false, originalList);
        }
        //获取不存在的页面数据不足时
        if ((index - 1) * pageSize > originalList.size()) {
            return Pair.of(false, Lists.newArrayList());
        }
        // 分页后的结果
        List<T> resultList = new ArrayList<>();
        // 如果需要进行分页
        // 获取起点
        long pageStart = (index - 1) * pageSize;
        // 获取终点
        long pageStop = pageStart + pageSize;
        //是否还有数据
        boolean hasMore = true;
        // 开始遍历
        while (pageStart < pageStop) {
            // 考虑到最后一页可能不够pageSize
            if (pageStart == originalList.size()) {
                hasMore = false;
                break;
            }
            resultList.add(originalList.get(Math.toIntExact(pageStart++)));
        }
        //请求数据刚好等于全部数据的情况
        if (index * pageSize == originalList.size()) {
            hasMore = false;
        }
        return Pair.of(hasMore, resultList);
    }


    /**
     * 逻辑分页,limit方式（第几个起到第几个的方式）
     *
     * @param originalList 分页前数据
     * @param index        页码
     * @param pageSize     每页数量
     * @return 分页后结果
     */
    public static <T> Pair<Boolean, List<T>> getPageV2(List<T> originalList, long index, long pageSize) {
        if (pageSize <= 0) {
            pageSize = 20L;
        }
        if (index <= 0) {
            index = 0L;
        }
        //获取数据位置大于总数据量
        if (index >= originalList.size()) {
            return Pair.of(false, Lists.newArrayList());
        }
        List<T> resultList = new ArrayList<>();
        // 如果需要进行分页
        // 获取起点
        long pageStart = index;
        // 获取终点
        long pageStop = index + pageSize;
        //是否还有数据
        boolean hasMore = true;
        // 开始遍历
        while (pageStart < pageStop) {
            // 考虑到最后一页可能不够pageSize
            if (pageStart == originalList.size()) {
                hasMore = false;
                break;
            }
            resultList.add(originalList.get(Math.toIntExact(pageStart++)));
        }
        return Pair.of(hasMore, resultList);
    }

}
