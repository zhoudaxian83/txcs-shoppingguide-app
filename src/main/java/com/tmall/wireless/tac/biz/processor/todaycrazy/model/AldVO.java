package com.tmall.wireless.tac.biz.processor.todaycrazy.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.tmall.txcs.gs.framework.model.EntityVO;
import lombok.Data;

@Data
public class AldVO<T extends EntityVO>{

    private String datasetId;
    private String contentId;
    private String startTime;
    private String numberValue;
    private List<T> itemAndContentList;
    private Boolean isHit;
    private Integer __pos__;

}
