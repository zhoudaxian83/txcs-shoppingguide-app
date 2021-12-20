package com.tmall.wireless.tac.biz.processor.processtemplate.timelimitedseckill.dto;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SecKillSessionDTO {

    public static final SecKillSessionDTO PLACEHOLDER_SEC_KILL_SESSION = new SecKillSessionDTO(-1);

    private String contentId;
    private String sessionTime;
    private String sessionText;
    private String startTime;
    private String endTime;
    private Long countDownMillis;
    private int status;
    private boolean selected;
    private String itemSet;
    private List<Map<String, Object>> items;

    public SecKillSessionDTO() {
    }

    public SecKillSessionDTO(int status) {
        this.status = status;
    }

    public GeneralItem toGeneralItem() {
        GeneralItem generalItem = new GeneralItem();
        generalItem.put("contentId", contentId);
        generalItem.put("sessionTime", sessionTime);
        generalItem.put("sessionText", sessionText);
        generalItem.put("startTime", startTime);
        generalItem.put("endTime", endTime);
        generalItem.put("countDownMillis", countDownMillis);
        generalItem.put("status", status);
        generalItem.put("selected", selected);
        generalItem.put("itemSet", itemSet);
        generalItem.put("items", items);
        return generalItem;
    }
}
