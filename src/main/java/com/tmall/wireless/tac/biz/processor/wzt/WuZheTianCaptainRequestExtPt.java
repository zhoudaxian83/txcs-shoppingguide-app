package com.tmall.wireless.tac.biz.processor.wzt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.alibaba.cola.extension.Extension;

import com.alibaba.fastjson.JSON;
import com.tmall.txcs.biz.supermarket.scene.util.MapUtil;
import com.tmall.txcs.gs.framework.extensions.iteminfo.request.CaptainRequestExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContext;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.biz.context.UserDO;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.model.spi.model.DataTubeParams;
import com.tmall.txcs.gs.model.spi.model.ItemDataRequest;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.wzt.constant.Constant;
import com.tmall.wireless.tac.biz.processor.wzt.utils.SmAreaIdUtil;
import com.tmall.wireless.tac.biz.processor.wzt.utils.TairUtil;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author luojunchong
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
        useCase = ScenarioConstantApp.LOC_TYPE_B2C,
        scenario = ScenarioConstantApp.WU_ZHE_TIAN)
@Service
public class WuZheTianCaptainRequestExtPt implements CaptainRequestExtPt {

    @Autowired
    TacLogger tacLogger;

    @Override
    public ItemDataRequest process(SgFrameworkContextItem sgFrameworkContextItem) {
        ItemDataRequest itemDataRequest = new ItemDataRequest();
        Long getSmAreaId = SmAreaIdUtil.getSmAreaId(sgFrameworkContextItem);
        Map<String, Object> userParam = sgFrameworkContextItem.getUserParams();
        Long storeId = MapUtil.getLongWithDefault(userParam, "captainStoreId", 0L);
        String O2oType = MapUtil.getStringWithDefault(userParam, "captainO2oType", "B2C");
        String mktSceneCode = MapUtil.getStringWithDefault(userParam, "captainSceneCode", "");
        List<ItemEntity> list = (List<ItemEntity>) userParam.get("captainItemEntityList");
        DataTubeParams dataTubeParams = (DataTubeParams) userParam.get("captainDataTubeParams");

        if (CollectionUtils.isEmpty(list)) {
            return itemDataRequest;
        }
        itemDataRequest.setList(list);
        if (storeId > 0L) {
            itemDataRequest.setStoreId(storeId);
        }
        itemDataRequest.setO2oType(O2oType);
        itemDataRequest.setUserId(Optional.ofNullable(sgFrameworkContextItem).map(SgFrameworkContext::getUserDO)
                .map(UserDO::getUserId).orElse(0L));
        itemDataRequest.setSmAreaId(getSmAreaId);
        if (StringUtils.isNotEmpty(mktSceneCode)) {
            itemDataRequest.setMktSceneCode(mktSceneCode);
        }
        if (dataTubeParams != null) {
            itemDataRequest.setDataTubeParams(dataTubeParams);
        }
        //itemDataRequest.setChannelKey(tairUtil.getChannelKey(getSmAreaId));Constant.CHANNEL_KEY
        itemDataRequest.setChannelKey((String) sgFrameworkContextItem.getUserParams().get(Constant.CHANNEL_KEY));
        if (Constant.DEBUG) {
            tacLogger.info("captain入参:" + JSON.toJSONString(itemDataRequest));
        }
        return itemDataRequest;
    }
}
