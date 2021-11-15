package com.tmall.wireless.tac.biz.processor.brandclub.bangdan;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.ext.annotation.SdkExtension;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.content.origindata.ContentOriginDataRequestBuildSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.context.*;
import com.tmall.txcs.gs.model.constant.RpmContants;
import com.tmall.wireless.store.spi.recommend.model.RecommendRequest;
import com.tmall.wireless.tac.biz.processor.brandclub.fp.BrandContentSetIdService;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.enums.TppItemBusinessTypeEnum;
import com.tmall.wireless.tac.biz.processor.firstScreenMind.utils.RenderLangUtil;
import com.tmall.wireless.tac.client.domain.Context;
import com.tmall.wireless.tac.client.domain.Enviroment;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SdkExtension(
        bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.BRAND_CLUB_BANGDAN
)
@Service
public class BrandClubBangdanContentOriginDataRequestBuildSdkExtPt extends Register implements ContentOriginDataRequestBuildSdkExtPt {

    @Autowired
    BrandContentSetIdService brandContentSetIdService;
    @Override
    public RecommendRequest process(SgFrameworkContextContent sgFrameworkContextContent) {

        Long topContentIdList = Optional.of(sgFrameworkContextContent)
                .map(SgFrameworkContext::getTacContext)
                .map(Context::getParams)
                .map(m -> m.get("topContentIdList"))
                .map(Object::toString).filter(StringUtils::isNumeric).map(Long::valueOf).orElse(0L);

        Long brandId = Optional.of(sgFrameworkContextContent)
                .map(SgFrameworkContext::getTacContext)
                .map(Context::getParams)
                .map(m -> m.get("brandId"))
                .map(Object::toString).filter(StringUtils::isNumeric).map(Long::valueOf).orElse(0L);

        Map<String, Map<String, Object>> groupAndBrandMapping =
                brandContentSetIdService.getGroupAndBrandMapping(Lists.newArrayList(brandId));

        List<Integer> contentSetIdList = groupAndBrandMapping.values().stream().findFirst()
                .map(m -> m.get("boardSceneGroupIds")).map(o -> {
                    List<Integer> contentSetIds = (List<Integer>) o;
                    return contentSetIds;
                }).orElse(Lists.newArrayList());


        RecommendRequest tppRequest = new RecommendRequest();
        Map<String, String> params = Maps.newHashMap();
        tppRequest.setParams(params);
        tppRequest.setLogResult(true);
        tppRequest.setUserId(Optional.of(sgFrameworkContextContent).
                map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserDO)
                .map(UserDO::getUserId).orElse(0L));
        tppRequest.setAppId(27402L);


        List<String> newContentSetIdList = contentSetIdList.stream().map(id -> "intelligentCombinationItems_" + id)
                .collect(
                        Collectors.toList());
        if(CollectionUtils.isEmpty(newContentSetIdList)){
            LOGGER.error("BrandClubBangdanContentOriginDataRequestBuildSdkExtPt getSceneSetBangDan empty. brandId:{}", brandId);
        }
        params.put("sceneSet", Joiner.on(",").join(newContentSetIdList));
        params.put("brandId", String.valueOf(brandId));
        params.put("sceneTop", String.valueOf(topContentIdList));
        /**心智场景支持O2O场景**/
        Long oneHour = Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getRt1HourStoreId).orElse(0L);
        Long halfDay = Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getRtHalfDayStoreId).orElse(0L);
        List<String> itemBusinessTypeList = Lists.newArrayList(TppItemBusinessTypeEnum.B2C.getType());
        if (oneHour > 0) {
            itemBusinessTypeList.add(TppItemBusinessTypeEnum.O2O.getType());
            params.put("rtOneHourStoreId", RenderLangUtil.safeString(oneHour));
        } else if (halfDay > 0){
            itemBusinessTypeList.add(TppItemBusinessTypeEnum.O2O.getType());
            params.put("rtHalfDayStoreId", RenderLangUtil.safeString(halfDay));
        }
        params.put("commerce", Joiner.on(",").join(itemBusinessTypeList));


        params.put("regionCode", Joiner.on(",").join(Optional.of(sgFrameworkContextContent).map(
                SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams).map(LocParams::getLogicIdByPriority).orElse(Lists.newArrayList())));
        if (params.get("regionCode") == null || "".equals(params.get("regionCode"))) {
            params.put("regionCode", "107");
        }
        params.put("smAreaId", Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getLocParams)
                .map(LocParams::getSmAreaId).orElse(0L).toString());
        params.put("exposureDataUserId", Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams)
                .map(CommonUserParams::getUserDO).map(UserDO::getCna).orElse(""));
        String pageSize = params.put("pageSize", Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(
                CommonUserParams::getUserPageInfo).map(PageInfoDO::getPageSize).orElse(20).toString());
        Integer index = Optional.of(sgFrameworkContextContent).map(SgFrameworkContext::getCommonUserParams).map(CommonUserParams::getUserPageInfo).map(
                PageInfoDO::getIndex).orElse(0);
        params.put("index", String.valueOf(index));
        params.put("isFirstPage", index > 0 ? "false" : "true");
        if (Enviroment.PRE.equals(RpmContants.enviroment)) {
            params.put("_devEnv_", "1");
        }
        return tppRequest;
    }
}
