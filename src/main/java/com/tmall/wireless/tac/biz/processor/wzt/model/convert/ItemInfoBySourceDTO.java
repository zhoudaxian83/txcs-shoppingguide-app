package com.tmall.wireless.tac.biz.processor.wzt.model.convert;

import java.util.Map;

import lombok.Data;

/**
 * @Author: luoJunChong
 * @Date: 2021/5/20 14:13
 * description:
 */
@Data
public class ItemInfoBySourceDTO {

    Map<String, Object> itemInfoVO;
    ItemDTO itemDTO;

}
