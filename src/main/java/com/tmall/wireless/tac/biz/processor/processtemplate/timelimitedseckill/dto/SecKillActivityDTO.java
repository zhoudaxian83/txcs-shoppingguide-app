package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.dto;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class SecKillActivityDTO {
    List<SecKillSessionDTO> secKillSessionDTOList;

    public List<GeneralItem> toGeneralItemList() {
        List<GeneralItem> generalItemList = new ArrayList<>();
        if(CollectionUtils.isEmpty(secKillSessionDTOList)) {
            return generalItemList;
        }
        for (SecKillSessionDTO secKillSessionDTO : secKillSessionDTOList) {
            generalItemList.add(secKillSessionDTO.toGeneralItem());
        }
        return generalItemList;
    }

    public static SecKillActivityDTO mock(List<Map<String, Object>> items) {
        SecKillActivityDTO activityDTO = new SecKillActivityDTO();
        List<SecKillSessionDTO> secKillSessionDTOList = new ArrayList<>();
        activityDTO.setSecKillSessionDTOList(secKillSessionDTOList);
        SecKillSessionDTO dto1 = new SecKillSessionDTO();
        dto1.setContentId("1634337111138");
        dto1.setSessionTime("8:00");
        dto1.setSessionText("母婴专场");
        dto1.setStartTime("1637547199000");
        dto1.setEndTime("1638547199000");
        dto1.setCountDownMillis(8547199000L);
        dto1.setStatus(0);
        dto1.setSelected(true);
        dto1.setItemSet("415609");
        dto1.setItems(items);
        secKillSessionDTOList.add(dto1);
        return activityDTO;
    }
}
