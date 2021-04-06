package com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.uitl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmall.txcs.gs.model.item.O2oType;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model.BannerDTO;
import com.tmall.wireless.tac.biz.processor.firstpage.banner.iteminfo.model.BannerItemDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by yangqing.byq on 2021/4/6.
 */

public class BannerUtil {

    public static Map<String, List<BannerItemDTO>> parseBannerItem(String bannerInfo) {
        Map<String, List<BannerItemDTO>> result = Maps.newHashMap();

        JSONObject jsonObject = JSON.parseObject(bannerInfo);

        jsonObject.keySet().forEach(key -> {

            List<BannerItemDTO> bannerItemDTOList = Lists.newArrayList();
            result.put(key, bannerItemDTOList);

            JSONObject banner = jsonObject.getJSONObject(key);
            BannerDTO bannerDTO = JSON.parseObject(banner.toJSONString(), BannerDTO.class);
            String locType = bannerDTO.getLocType();
            locType = StringUtils.isEmpty(locType) ? O2oType.B2C.name() : locType;
            O2oType o2oType = O2oType.from(locType);

            if (CollectionUtils.isEmpty(bannerDTO.getItemIdList())) {
                return;
            }
            bannerDTO.getItemIdList().forEach(itemId -> {
                BannerItemDTO bannerItemDTO = new BannerItemDTO();
                bannerItemDTO.setLocType(o2oType.name());
                bannerItemDTO.setItemId(itemId);
                bannerItemDTOList.add(bannerItemDTO);
            });
        });
        return result;
    }
}
