package com.tmall.wireless.tac.biz.processor.firstScreenMind.enums;

import lombok.Getter;

public enum RenderErrorEnum {
    /***内容中心错误码***/
    contentTairCheckFail("contentTairCheckFail","内容中心tair参数检查失败"),
    contentTairExc("contentTairExc","内容中心tair发生异常"),
    contentTairFail("contentTairFail","内容中心tair发生失败"),
    contentTairValueNull("contentTairValueNull","内容中心tair值为空"),
    contentContentNull("contentContentNull","内容中心内容值为空"),

    contentBatchTairCheckFail("contentBatchTairCheckFail","内容中心批量tair参数检查失败"),
    contentBatchTairExc("contentBatchTairExc","内容中心批量tair发生异常"),
    contentBatchTairFail("contentBatchTairFail","内容中心批量tair发生失败"),
    contentBatchTairValueNull("contentBatchTairValueNull","内容中心批量tair值为空"),
    contentSingleTairFail("contentSingleTairFail","内容中心单个tair发生失败"),
    contentSingleTairValueNull("contentSingleTairValueNull","内容中心单个tair值为空"),
    contentSingleContentNull("contentSingleContentNull","内容中心单个内容值为空"),

    contentBottomTairCheckFail("contentBottomTairCheckFail","内容中心内容打底tair参数检查失败"),
    contentBottomTairExc("contentBottomTairExc","内容中心内容打底tair发生异常"),
    contentBottomTairFail("contentBottomTairFail","内容中心内容打底tair发生失败"),
    contentBottomTairValueNull("contentBottomTairValueNull","内容中心内容打底tair值为空"),
    contentBottomContentListNull("contentBottomContentListNull","内容中心打底内容列表为空"),

    contentBatchBottomTairCheckFail("contentBatchBottomTairCheckFail","内容中心内容批量打底tair参数检查失败"),
    contentBatchBottomTairExc("contentBatchBottomTairExc","内容中心内容批量打底tair发生异常"),
    contentBatchBottomTairFail("contentBatchBottomTairFail","内容中心内容批量打底tair发生失败"),
    contentBatchBottomContentMapNull("contentBatchBottomContentMapNull","内容中心批量打底内容map为空"),
    contentSingleBottomTairFail("contentSingleBottomTairFail","内容中心单个打底tair发生失败"),
    contentSingleBottomTairValueNull("contentSingleBottomTairValueNull","内容中心单个内容打底tair值为空"),
    contentSingleBottomTairValueListNull("contentSingleBottomTairValueListNull","内容中心单个内容打底tair值列表为空"),

    contentItemBottomTairCheckFail("contentItemBottomTairCheckFail","内容中心商品打底tair参数检查失败"),
    contentItemBottomTairExc("contentItemBottomTairExc","内容中心商品打底tair发生异常"),
    contentItemBottomTairFail("contentItemBottomTairFail","内容中心商品打底tair发生失败"),
    contentItemBottomTairValueNull("contentItemBottomTairValueNull","内容中心商品打底tair值为空"),
    contentItemBottomItemListNull("contentItemBottomItemListNull","内容中心打底商品列表为空"),

    contentTopContentSetExc("contentTopContentSetExc","内容中心获取Top内容集发生异常"),
    contentTopContentSetValueNull("contentTopContentSetValueNull","内容中心Top内容集ID列表返回为空"),

    OTHER("renderError","未知的错误类型");
    @Getter
    private String code ;

    @Getter
    private String message ;

    private RenderErrorEnum(String code, String message) {
        this.message = message;
        this.code = code;
    }

    public static String getErrorMessageDescByCode(String code){
        for(RenderErrorEnum aselfRenderErrorEnum : RenderErrorEnum.values()){
            if(aselfRenderErrorEnum.code.equals(code)){
                return aselfRenderErrorEnum.message;
            }
        }
        return RenderErrorEnum.OTHER.message;
    }
}
