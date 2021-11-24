package com.tmall.wireless.tac.biz.processor.icon.model;

import java.util.Date;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

@Data
public class IconFixedItemDTO {

    private Long itemId;
    private Date beginTime;
    private Date endTime;
    private Long index;

    public static List<IconFixedItemDTO> getFixedItemByStr(String fixedItemStr) {
        if (StringUtils.isBlank(fixedItemStr)) {
            return Lists.newArrayList();
        }
        List<IconFixedItemDTO> fixedItemVOS = Lists.newArrayList();
        Splitter.on(";").split(fixedItemStr).forEach(singleFixedItemStr -> {
            IconFixedItemDTO fixedItemVO = new IconFixedItemDTO();
            String[] fixedItemOps = singleFixedItemStr.split(":");
            if (fixedItemOps.length == 0) {
                return;
            }
            if (!(StringUtils.isNumeric(fixedItemOps[0])
                && StringUtils.isNumeric(fixedItemOps[1])
                && StringUtils.isNumeric(fixedItemOps[2])
                && StringUtils.isNumeric(fixedItemOps[3]))) {
                return;
            }
            fixedItemVO.setItemId(Long.valueOf(fixedItemOps[0]));
            fixedItemVO.setIndex(Long.valueOf(fixedItemOps[1]));
            fixedItemVO.setBeginTime(new Date(Long.parseLong(fixedItemOps[2])));
            fixedItemVO.setEndTime(new Date(Long.parseLong(fixedItemOps[3])));
            fixedItemVOS.add(fixedItemVO);
        });
        return fixedItemVOS;
    }
}
