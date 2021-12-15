package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.dto;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.util.ItemUtil;
import com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain.SecKillActivity;
import com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain.SecKillSession;
import com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.domain.SelectedSecKillSession;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
public class SecKillActivityDTO {
    List<SecKillSessionDTO> secKillSessionDTOList;

    public static SecKillActivityDTO valueOf(SecKillActivity secKillActivity, SelectedSecKillSession selectedSecKillSession, List<Long> allItemIds, Map<Long, ItemDTO> longItemDTOMap) {
        SecKillActivityDTO activityDTO = new SecKillActivityDTO();
        List<SecKillSessionDTO> secKillSessionDTOList = new ArrayList<>();
        activityDTO.setSecKillSessionDTOList(secKillSessionDTOList);
        List<SecKillSession> secKillSessions = secKillActivity.validSecKillSessions();
        if(CollectionUtils.isEmpty(secKillSessions)) {
            return activityDTO;
        }
        for (int i=0; i<secKillSessions.size(); i++) {
            SecKillSession secKillSession = secKillSessions.get(i);
            SecKillSessionDTO secKillSessionDTO = new SecKillSessionDTO();
            secKillSessionDTO.setContentId(String.valueOf(secKillSession.id()));
            secKillSessionDTO.setSessionTime(secKillSession.parseHHMMofStartTime());
            secKillSessionDTO.setSessionText(secKillSession.sessionText());
            secKillSessionDTO.setStartTime(String.valueOf(secKillSession.startTimestamps()));
            secKillSessionDTO.setEndTime(String.valueOf(secKillSession.endTimestamps()));
            //如果选中的场次是第一个场次才有倒计时
            if(i == 0 && Objects.equals(secKillSession.id(), selectedSecKillSession.id())) {
                secKillSessionDTO.setCountDownMillis(secKillSession.countDownMillis());
            }
            secKillSessionDTO.setStatus(secKillSession.status());
            if(Objects.equals(secKillSession.id(), selectedSecKillSession.id())) {
                secKillSessionDTO.setSelected(true);
                secKillSessionDTO.setItems(ItemUtil.buildItems(allItemIds, longItemDTOMap));
            } else {
                secKillSessionDTO.setSelected(false);
            }
            secKillSessionDTO.setItemSet(secKillSession.itemSetId());
            secKillSessionDTOList.add(secKillSessionDTO);
        }
        return activityDTO;
    }

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

}
