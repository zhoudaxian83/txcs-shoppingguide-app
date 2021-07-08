package com.tmall.wireless.tac.biz.processor.firstScreenMind;

import java.util.Optional;

import com.alibaba.cola.extension.Extension;

import com.tmall.txcs.biz.supermarket.iteminfo.source.captain.ItemInfoBySourceDTOMain;
import com.tmall.txcs.gs.framework.extensions.buildvo.BuildItemVOExtPt;
import com.tmall.txcs.gs.framework.extensions.buildvo.BuildItemVoRequest;
import com.tmall.txcs.gs.framework.model.ErrorCode;
import com.tmall.txcs.gs.framework.model.ItemEntityVO;
import com.tmall.txcs.gs.framework.model.constant.ScenarioConstant;
import com.tmall.txcs.gs.model.Response;
import com.tmall.txcs.gs.model.spi.model.ItemDataDTO;
import com.tmall.txcs.gs.model.spi.model.ItemInfoBySourceDTO;
import com.tmall.txcs.gs.model.spi.model.ItemInfoDTO;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import org.springframework.stereotype.Service;

/**
 * Created by yangqing.byq on 2021/5/1.
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
@Service
public class FirstScreenMindItemInfoBuildItemVOExtPt implements BuildItemVOExtPt {
    @Override
    public Response<ItemEntityVO> process(BuildItemVoRequest buildItemVoRequest) {
        ItemEntityVO itemEntityVO = new ItemEntityVO();
        itemEntityVO.put("contentType", 0);
        boolean hasMainSource = false;
        boolean canBuy = true;

        if (buildItemVoRequest == null || buildItemVoRequest.getItemInfoDTO() == null) {
            return Response.fail(ErrorCode.PARAMS_ERROR);
        }

        ItemInfoDTO itemInfoDTO = buildItemVoRequest.getItemInfoDTO();

        for (String s : itemInfoDTO.getItemInfos().keySet()) {
            ItemInfoBySourceDTO itemInfoBySourceDTO = itemInfoDTO.getItemInfos().get(s);
            if (itemInfoBySourceDTO instanceof ItemInfoBySourceDTOMain) {
                hasMainSource = true;
                canBuy = canBuy((ItemInfoBySourceDTOMain)itemInfoBySourceDTO);
            }
//            if (itemInfoBySourceDTO instanceof ItemInfoBySourceDTOInv) {
//                canBuy = ((ItemInfoBySourceDTOInv) itemInfoBySourceDTO).isCanBuy();
//            }
            itemEntityVO.putAll(itemInfoBySourceDTO.getItemInfoVO());
        }

        if (!canBuy) {
            return Response.fail("ITEM_VO_BUILD_ERROR_CAN_BUY_FALSE_F");
        }

        if (!hasMainSource) {
            return Response.fail(ErrorCode.ITEM_VO_BUILD_ERROR_HAS_NO_MAIN_SOURCE);
        }
        return Response.success(itemEntityVO);
    }

    private boolean canBuy(ItemInfoBySourceDTOMain itemInfoBySourceDTO) {
        Boolean canBuy = Optional.of(itemInfoBySourceDTO).map(ItemInfoBySourceDTOMain::getItemDTO).map(ItemDataDTO::isCanBuy).orElse(true);
        Boolean sellOut = Optional.of(itemInfoBySourceDTO).map(ItemInfoBySourceDTOMain::getItemDTO).map(ItemDataDTO::isSellOut).orElse(false);
        return canBuy && !sellOut;

    }
}
