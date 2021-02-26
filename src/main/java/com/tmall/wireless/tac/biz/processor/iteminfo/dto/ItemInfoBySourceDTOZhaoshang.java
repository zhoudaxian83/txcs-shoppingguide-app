package com.tmall.wireless.tac.biz.processor.iteminfo.dto;

import com.tmall.txcs.gs.framework.support.itemInfo.bysource.ItemInfoBySourceDTO;
import lombok.Data;
import org.springframework.stereotype.Service;

/**
 * Created by yangqing.byq on 2021/2/26.
 */
@Data
public class ItemInfoBySourceDTOZhaoshang extends ItemInfoBySourceDTO {
    String zhaoshangInfo;
}
