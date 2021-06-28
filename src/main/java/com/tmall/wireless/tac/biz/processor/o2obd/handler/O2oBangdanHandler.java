package com.tmall.wireless.tac.biz.processor.o2obd.handler;

import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.wireless.tac.biz.processor.config.SxlSwitch;
import com.tmall.wireless.tac.biz.processor.o2obd.service.O2oBangdanService;
import com.tmall.wireless.tac.client.common.TacResult;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import com.tmall.wireless.tac.client.domain.RequestContext4Ald;
import com.tmall.wireless.tac.client.handler.TacReactiveHandler4Ald;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author haixiao.zhang
 * @date 2021/6/22
 */
@Service
public class O2oBangdanHandler extends TacReactiveHandler4Ald {

    @Autowired
    O2oBangdanService o2oBangdanService;

    @Autowired
    TacLogger tacLogger;

    @Override
    public Flowable<TacResult<List<GeneralItem>>> executeFlowable(RequestContext4Ald requestContext4Ald)
        throws Exception {

        /**
         * https://pages.tmall.com/wow/an/cs/act/wupr?spm=a3204.21125900.9715263030.d_b2cNormalContent_2020053172898_19539059607&wh_biz=tm&wh_pid=go-shopping/1774bde7dd7&disableNav=YES&recommendForUTab=true&b2cContentSetIds=7002&o2oContentSetIds=17002&mediaContentSetIds=140002&rankingContentSetIds=160002&contentId=2020053172898&contentType=b2cNormalContent&itemSetIds=210671&entryItemIds=19539059607,12424825277,39352890583,19555059312,41763548231,17059283658
         */
        /**
         *           "contentAuthor": null,
         *           "contentBackgroundPic": "1",
         *           "contentCustomLink": null,
         *           "contentSubtitle": "测试O2O榜单-华东",
         *           "contentPic": "1",
         *           "contentSeeCount": null,
         *           "contentId": "2020053217732",
         *           "contentTitle": "测试O2O榜单-华东",
         *           "contentVideoUrl": null,
         *           "contentType": "bangdanContent",
         *           "items": [
         *           ],
         *           "itemSetIds": "373479",
         *            "__track__": "13753845.13753845.20719161.1914.2"
         */
        return o2oBangdanService.recommend(requestContext4Ald).map(response->{
            List<GeneralItem> generalItemList = Lists.newArrayList();
            List<ContentVO> list = response.getData().getItemAndContentList();
            list.forEach(contentVO -> {
                GeneralItem generalItem = new GeneralItem();
                contentVO.keySet().forEach(key->{
                    generalItem.put(key,contentVO.get(key));
                });
                generalItem.put("jumpUrl",buildJumpUrl(generalItem));
                generalItemList.add(generalItem);
            });
            return generalItemList;
        }).map(TacResult::newResult)
        .onErrorReturn((r -> TacResult.errorResult("")));

    }

    private String buildJumpUrl(GeneralItem generalItem){

        String contentId = generalItem.getString("contentId");
        String contentType = generalItem.getString("contentType");
        String itemSetIds = generalItem.getString("itemSetIds");
        List<EntityVO> itemList = (List<EntityVO>)generalItem.get("items");
        List<String> itemIdList = itemList.stream().map(entityVO -> {
            return entityVO.get("itemId");
        }).map(String::valueOf).collect(Collectors.toList());

        String jumpUrl = (String)JSON.parse(SxlSwitch.getValue("O2O_BD_JUMP_UTL"));
        tacLogger.info("buildJumpUrl:"+jumpUrl);
        return String.format(jumpUrl,contentId,contentType,itemSetIds,String.join(",",itemIdList));
    }

    public static void main(String args[]){

        System.out.println(String.format(SxlSwitch.O2O_BD_JUMP_UTL,"2020053217732","bangdanContent","373479","111"));

        List<String> itemList = Lists.newArrayList();
        itemList.add("11111");
        itemList.add("2222");

        System.out.println(String.join(",",itemList));

        String js = JSON.toJSONString(SxlSwitch.O2O_BD_JUMP_UTL);

        System.out.println(js);
        String aa = (String)JSON.parse(js);
        System.out.println(aa);



    }
}

