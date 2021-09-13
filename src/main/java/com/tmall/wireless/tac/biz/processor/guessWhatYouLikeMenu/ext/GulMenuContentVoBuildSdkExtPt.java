package com.tmall.wireless.tac.biz.processor.guessWhatYouLikeMenu.ext;

import com.google.common.collect.Lists;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.excute.ExtensionPointExecutor;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.vo.ContentVoBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoRequest;
import com.tmall.tcls.gs.sdk.framework.extensions.item.vo.BuildItemVoSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.Response;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.tcls.gs.sdk.framework.suport.LogUtil;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Yushan
 * @date 2021/9/13 5:31 下午
 */
@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.CNXH_MENU_FEEDS
)
public class GulMenuContentVoBuildSdkExtPt extends Register implements ContentVoBuildSdkExtPt {

    @Resource
    ExtensionPointExecutor extensionPointExecutor;
    @Override
    public SgFrameworkResponse<ContentVO> process(SgFrameworkContextContent sgFrameworkContextContent) {

        List<ContentDTO> contentDTOList = sgFrameworkContextContent.getContentDTOList();

        List<ContentVO> contentVOS = buildContentListVO(sgFrameworkContextContent);
        SgFrameworkResponse<ContentVO> sgFrameworkResponse = new SgFrameworkResponse<>();
        sgFrameworkResponse.setSuccess(true);
        sgFrameworkResponse.setHasMore(Optional.of(sgFrameworkContextContent)
                .map(SgFrameworkContextContent::getContentEntityOriginDataDTO)
                .map(OriginDataDTO::isHasMore).orElse(false));
        sgFrameworkResponse.setIndex(contentDTOList == null ? 0 : contentDTOList.size());
        sgFrameworkResponse.setItemAndContentList(contentVOS);
        return sgFrameworkResponse;
    }

    private List<ContentVO> buildContentListVO(SgFrameworkContextContent sgFrameworkContextContent) {


        List<ContentDTO> contentDTOList = sgFrameworkContextContent.getContentDTOList();

        List<ContentEntity> contentEntities = Optional.of(sgFrameworkContextContent).map(SgFrameworkContextContent::getContentEntityOriginDataDTO).map(OriginDataDTO::getResult).orElse(Lists.newArrayList());

        if (CollectionUtils.isEmpty(contentDTOList) || CollectionUtils.isEmpty(contentEntities)) {
            return Lists.newArrayList();
        }
        Map<Long, ContentDTO> longContentDTOMap = contentDTOList.stream().collect(Collectors.toMap(ContentDTO::getContentId, c -> c));


        return contentEntities.stream().map(contentEntity -> {
            ContentDTO contentDTO = longContentDTOMap.get(contentEntity.getContentId());
            if (contentDTO == null) {
                return null;
            }
            ContentVO contentVO = new ContentVO();
            contentVO.put("contentId", contentDTO.getContentId());
            contentVO.put("items", processItemList(contentEntity, contentDTO, sgFrameworkContextContent));
            if (MapUtils.isNotEmpty(contentDTO.getContentInfo())) {
                contentVO.putAll(contentDTO.getContentInfo());
            }
            return contentVO;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<ItemEntityVO> processItemList(ContentEntity contentEntity, ContentDTO contentDTO, SgFrameworkContextContent sgFrameworkContextContent) {


        List<ItemEntity> items = contentEntity.getItems();
        List<ItemInfoDTO> itemInfoDTOList = contentDTO.getItemInfoDTOList();

        if (CollectionUtils.isEmpty(items) || CollectionUtils.isEmpty(itemInfoDTOList)) {
            return Lists.newArrayList();
        }

        Map<ItemUniqueId, ItemInfoDTO> itemUniqueIdItemInfoDTOMap = itemInfoDTOList.stream().collect(Collectors.toMap(itemInfoDTO -> itemInfoDTO.getItemEntity().getItemUniqueId(), i -> i, (entity1, entity2) -> entity1));


        SgFrameworkContextItem sgFrameworkContextItem = new SgFrameworkContextItem();
        sgFrameworkContextItem.setItemMetaInfo(sgFrameworkContextContent.getItemMetaInfo());
        sgFrameworkContextItem.setCommonUserParams(sgFrameworkContextContent.getCommonUserParams());
        sgFrameworkContextItem.setBizScenario(sgFrameworkContextContent.getBizScenario());
        sgFrameworkContextItem.setTacContext(sgFrameworkContextContent.getTacContext());

        return items.stream().map(itemEntity -> {
            ItemInfoDTO itemInfoDTO = itemUniqueIdItemInfoDTOMap.get(itemEntity.getItemUniqueId());
            if (itemInfoDTO == null) {
                return null;
            }
            BuildItemVoRequest buildItemVoRequest = new BuildItemVoRequest();
            buildItemVoRequest.setItemInfoDTO(itemInfoDTO);
            buildItemVoRequest.setContext(sgFrameworkContextItem);
            Response<ItemEntityVO> itemEntityVOResponse = extensionPointExecutor.execute(BuildItemVoSdkExtPt.class,
                    sgFrameworkContextContent.getBizScenario(),
                    pt -> pt.process0(buildItemVoRequest)
            );
            if (itemEntityVOResponse.isSuccess()) {
                return itemEntityVOResponse.getValue();
            } else {
                LogUtil.error(sgFrameworkContextContent.getBizScenario().getUniqueIdentity(),
                        LogUtil.STEP_KEY_BUILD_ITEM_VO,
                        "",
                        itemEntityVOResponse.getErrorCode(),
                        itemEntityVOResponse.getErrorMsg()
                );
                return null;
            }

        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
