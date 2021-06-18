package com.tmall.wireless.tac.biz.processor.wzt.utils;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class LogicPageUtil {

    /**
     * 手动分页
     *
     * @param originalList 分页前数据
     * @param index        页码
     * @param pageSize     每页数量
     * @return 分页后结果
     */
    public static <T> List<T> getPage(List<T> originalList, Long index, Long pageSize) {
        if (index < 1) {
            index = 1L;
        }
        //第一页，每页数据大于总数据时全部返回
        if (index == 1 && pageSize > originalList.size()) {
            return originalList;
        }
        if (index * pageSize > originalList.size()) {
            return Lists.newArrayList();
        }
        // 分页后的结果
        List<T> resultList = new ArrayList<>();
        // 如果需要进行分页
        if (pageSize > 0) {
            // 获取起点
            long pageStart = (index - 1) * pageSize;
            // 获取终点
            long pageStop = pageStart + pageSize;
            // 开始遍历
            while (pageStart < pageStop) {
                // 考虑到最后一页可能不够pageSize
                if (pageStart == originalList.size()) {
                    break;
                }
                resultList.add(originalList.get(Math.toIntExact(pageStart++)));
            }
        }
        // 如果不进行分页，显示所有数据
        else {
            resultList = originalList;
        }
        return resultList;
    }
}
