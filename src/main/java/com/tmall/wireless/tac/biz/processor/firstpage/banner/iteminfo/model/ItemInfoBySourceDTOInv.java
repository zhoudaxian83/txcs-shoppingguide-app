package com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model;

import com.tmall.txcs.gs.model.spi.model.ItemInfoBySourceDTO;
import lombok.Data;

/**
 * Created by yangqing.byq on 2021/5/1.
 */
@Data
public class ItemInfoBySourceDTOInv extends ItemInfoBySourceDTO {
    boolean canBuy = true;
}
