package com.tmall.wireless.tac.biz.processor.icon.model;

import com.tmall.aself.shoppingguide.client.cat.model.LabelDTO;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import lombok.Data;

import java.util.List;

@Data
public class IconResponse {
    List<LabelDTO> secondList;
    List<LabelDTO> thrirdList;
    SgFrameworkResponse<ItemEntityVO> itemList;

}
