package com.tmall.wireless.tac.biz.processor.wzt.utils;

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
        return Pair.of(hasMore, resultList);
    }

}
