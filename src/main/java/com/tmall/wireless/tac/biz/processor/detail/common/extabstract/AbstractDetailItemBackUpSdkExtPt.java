package com.tmall.wireless.tac.biz.processor.detail.common.extabstract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.alibaba.fastjson.JSON;
import com.alibaba.metrics.StringUtils;

import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.tmall.tcls.gs.sdk.ext.extension.Register;
import com.tmall.tcls.gs.sdk.framework.extensions.item.filter.ItemProcessBeforeReturnSdkExtPt;
import com.tmall.tcls.gs.sdk.framework.model.ItemEntityVO;
import com.tmall.tcls.gs.sdk.framework.model.context.SgFrameworkContextItem;
import com.tmall.tmallwireless.tac.spi.context.SPIResult;
import com.tmall.wireless.store.spi.tair.TairSpi;
import com.tmall.wireless.tac.biz.processor.detail.common.config.DetailSwitch;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.DetailConstant;
import com.tmall.wireless.tac.biz.processor.detail.common.constant.TppParmasConstant;
import com.tmall.wireless.tac.biz.processor.detail.model.DetailRecommendRequest;
import com.tmall.wireless.tac.biz.processor.detail.model.config.DetailRequestConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author: guichen
 * @Data: 2021/9/28
 * @Description:
 */
public class AbstractDetailItemBackUpSdkExtPt extends Register implements ItemProcessBeforeReturnSdkExtPt {
    public static final String DETAIL_TAIR_USER_NAME = "b2141a8eda7e4181";
    public static final int DETAIL_NAME_SPACE = 10200;

    Logger logger = LoggerFactory.getLogger(AbstractDetailItemBackUpSdkExtPt.class);

    @Autowired
    TairSpi tairSpi;

    @Override
    public SgFrameworkContextItem process(SgFrameworkContextItem sgFrameworkContextItem) {

        DetailRecommendRequest detailRequest = DetailRecommendRequest.getDetailRequest(
            sgFrameworkContextItem.getTacContext());
        DetailRequestConfig detailRequestConfig = DetailSwitch.requestConfigMap.get(detailRequest.getRecType());

        if (!detailRequestConfig.isOpenBackUp()) {
            return sgFrameworkContextItem;
        }

        if (StringUtils.isBlank(backUpCacheKey(sgFrameworkContextItem))) {
            return sgFrameworkContextItem;
        }

        if (isFullItemLength(sgFrameworkContextItem, detailRequestConfig)) {
            doBackUp(sgFrameworkContextItem, detailRequestConfig);
        } else {
            fillBackUpResult(sgFrameworkContextItem, detailRequestConfig);
        }

        return sgFrameworkContextItem;
    }

    private void doBackUp(SgFrameworkContextItem sgFrameworkContextItem, DetailRequestConfig detailRequestConfig) {

        if (System.currentTimeMillis() % 10000 < detailRequestConfig.getBackUpWriteRate()) {
            return;
        }

        try {
            String cacheKey = backUpCacheKey(sgFrameworkContextItem);
            List<ItemEntityVO> itemAndContentList = sgFrameworkContextItem.getEntityVOSgFrameworkResponse()
                .getItemAndContentList();

            SPIResult<ResultCode> put = tairSpi.put(DETAIL_TAIR_USER_NAME, DETAIL_NAME_SPACE, cacheKey,
                JSON.toJSONString(itemAndContentList));
            if (!put.isSuccess()) {
                logger.error("detail.doBackUp error,tppId:{},error:{}", detailRequestConfig.getTppId(),
                    put.getMsgInfo());
            }
        } catch (Throwable e) {
            logger.error("detail.doBackUp error,tppId:{},error:{}", detailRequestConfig.getTppId(), e);
        }

    }

    private boolean isFullItemLength(SgFrameworkContextItem sgFrameworkContextItem,
        DetailRequestConfig detailRequestConfig) {
        List<ItemEntityVO> itemAndContentList = sgFrameworkContextItem.getEntityVOSgFrameworkResponse()
            .getItemAndContentList();

        return !CollectionUtils.isEmpty(itemAndContentList) &&
            itemAndContentList.size() >= detailRequestConfig.getSizeDTO().getMin();
    }

    private String backUpCacheKey(SgFrameworkContextItem sgFrameworkContextItem) {

        return (String)sgFrameworkContextItem.getTacContext().getParams().get(DetailConstant.CACH_KEY);
    }

    private void fillBackUpResult(SgFrameworkContextItem sgFrameworkContextItem,
        DetailRequestConfig detailRequestConfig) {

        //是首页查询打底
        String firstPage = (String)sgFrameworkContextItem.getTacContext().getParams().get(
            TppParmasConstant.IS_FIRST_PAGE);
        if (!"true".equals(firstPage)) {
            return;
        }
        try {
            String cacheKey = backUpCacheKey(sgFrameworkContextItem);

            SPIResult<Result<DataEntry>> result = tairSpi.get(DETAIL_TAIR_USER_NAME, DETAIL_NAME_SPACE, cacheKey);

            if (!result.isSuccess()) {
                logger.error("detail.fillBackUpResult error,tppId:{},error:{}", detailRequestConfig.getTppId(),
                    result.getMsgInfo());
                return;
            }

            Optional.ofNullable(result.getData())
                .map(Result::getValue)
                .map(v -> (String)v.getValue())
                .filter(StringUtils::isNotBlank)
                .map(v -> JSON.parseArray(v, ItemEntityVO.class))
                .filter(CollectionUtils::isNotEmpty)
                .ifPresent(v -> {
                    List<ItemEntityVO> itemAndContentList = sgFrameworkContextItem.getEntityVOSgFrameworkResponse()
                        .getItemAndContentList();

                    if (CollectionUtils.isEmpty(itemAndContentList)) {
                        itemAndContentList = new ArrayList<>(v.size());
                    }

                    itemAndContentList.addAll(v);

                    sgFrameworkContextItem.getEntityVOSgFrameworkResponse().setItemAndContentList(itemAndContentList);

                });

        } catch (Throwable e) {
            logger.error("detail.fillBackUpResult error,tppId:{},error:{}", detailRequestConfig.getTppId(), e);
        }
    }
}