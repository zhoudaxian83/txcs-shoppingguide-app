package com.tmall.wireless.tac.biz.processor.common.util;

import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.tcls.gs.sdk.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.client.common.TacResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author guijian
 * tac通用打底-日志监控埋点
 */
public class TacResultBackupUtil {
    /**
     *
     * @param tacResult
     * @param bizScenario  业务身份必须传，否则无法根据业务身份监控
     * @return
     */
    public static TacResult tacResultBackup(TacResult<SgFrameworkResponse> tacResult, BizScenario bizScenario){
        if(bizScenario != null && StringUtils.isNotEmpty(bizScenario.getUniqueIdentity())){
            tacResult.getBackupMetaData().setUseBackup(true);
            return tacResult;
        }
        if(tacResult.getData() == null || tacResult.getData()== null || CollectionUtils.isEmpty(tacResult.getData().getItemAndContentList())){
            tacResult = TacResult.errorResult("TacResultBackup");

            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                .kv("key","tacBackup")
                .kv("tacResultBackup","true")
                .info();
        }else{
            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                .kv("key","tacBackup")
                .kv("tacResultBackup","false")
                .info();
        }
        tacResult.getBackupMetaData().setUseBackup(true);
        return tacResult;
    }

}
