package com.tmall.wireless.tac.biz.processor.common.util;

import java.util.List;
import com.alibaba.aladdin.lamp.domain.response.GeneralItem;
import com.alibaba.fastjson.JSON;

import com.tmall.hades.monitor.print.HadesLogUtil;
import com.tmall.tcls.gs.sdk.ext.BizScenario;
import com.tmall.txcs.gs.framework.model.ContentVO;
import com.tmall.txcs.gs.framework.model.EntityVO;
import com.tmall.txcs.gs.framework.model.SgFrameworkResponse;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.client.common.TacResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author guijian
 * tac通用打底-日志监控埋点
 */
public class TacResultBackupUtil {
    private static final String TAC_BACKUP_KEY = "tacBackup";
    /**
     *
     * @param tacResult 内容推荐
     * @param bizScenario  业务身份必须传，否则无法根据业务身份监控
     * @return
     */
    public static TacResult tacResultBackupContent(TacResult<SgFrameworkResponse<ContentVO>> tacResult, BizScenario bizScenario){
        if(bizScenario == null || StringUtils.isEmpty(bizScenario.getUniqueIdentity())){
            tacResult.getBackupMetaData().setUseBackup(true);
            return tacResult;
        }
        if(tacResult == null || tacResult.getData()== null || CollectionUtils.isEmpty(tacResult.getData().getItemAndContentList())){
            tacResult = TacResult.errorResult("TacResultBackup");

            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                    .kv("key",TAC_BACKUP_KEY)
                    .kv("tacResultBackup","true")
                    .info();
        }else{
            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                    .kv("key",TAC_BACKUP_KEY)
                    .kv("tacResultBackup","false")
                    .info();
        }
        tacResult.getBackupMetaData().setUseBackup(true);
        return tacResult;
    }
    /**
     *
     * @param tacResult 内容推荐--带参数的
     * @param bizScenario  业务身份必须传，否则无法根据业务身份监控
     * @return
     */
    public static TacResult tacResultBackupContentParam(TacResult<SgFrameworkResponse<ContentVO>> tacResult, BizScenario bizScenario
            ,String backupParams){
        if(bizScenario == null || StringUtils.isEmpty(bizScenario.getUniqueIdentity())){
            /**打底参数**/
            if(StringUtils.isNotEmpty(backupParams)){
                tacResult.getBackupMetaData().setBackupWithParam(true);
                tacResult.getBackupMetaData().setBackupKey(backupParams);
            }
            tacResult.getBackupMetaData().setUseBackup(true);
            return tacResult;
        }
        if(tacResult == null || tacResult.getData()== null || CollectionUtils.isEmpty(tacResult.getData().getItemAndContentList())){
            tacResult = TacResult.errorResult("TacResultBackup");

            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                .kv("key",TAC_BACKUP_KEY)
                .kv("tacResultBackup","true")
                .info();
        }else{
            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                .kv("key",TAC_BACKUP_KEY)
                .kv("tacResultBackup","false")
                .info();
        }
        /**打底参数**/
        if(StringUtils.isNotEmpty(backupParams)){
            tacResult.getBackupMetaData().setBackupWithParam(true);
            tacResult.getBackupMetaData().setBackupKey(backupParams);
            HadesLogUtil.stream(ScenarioConstantApp.SCENE_FIRST_SCREEN_MIND_CONTENT)
                .kv("TacResultBackupUtil","tacResultBackupContentParam")
                .kv("backupParams", JSON.toJSONString(backupParams))
                .info();
        }
        tacResult.getBackupMetaData().setUseBackup(true);
        return tacResult;
    }
    /**
     *
     * @param tacResult 商品推荐
     * @param bizScenario  业务身份必须传，否则无法根据业务身份监控
     * @return
     */
    public static TacResult tacResultBackupItem(TacResult<SgFrameworkResponse<EntityVO>> tacResult, BizScenario bizScenario){
        if(bizScenario == null || StringUtils.isEmpty(bizScenario.getUniqueIdentity())){
            tacResult.getBackupMetaData().setUseBackup(true);
            return tacResult;
        }
        if(tacResult == null || tacResult.getData()== null || CollectionUtils.isEmpty(tacResult.getData().getItemAndContentList())){
            tacResult = TacResult.errorResult("TacResultBackup");

            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                .kv("key",TAC_BACKUP_KEY)
                .kv("tacResultBackup","true")
                .info();
        }else{
            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                .kv("key",TAC_BACKUP_KEY)
                .kv("tacResultBackup","false")
                .info();
        }
        tacResult.getBackupMetaData().setUseBackup(true);
        return tacResult;
    }
    /**
     *
     * @param tacResult 商品推荐ald
     * @param bizScenario  业务身份必须传，否则无法根据业务身份监控
     * @return
     */
    public static TacResult tacResultBackupItemAld(TacResult<List<GeneralItem>> tacResult, BizScenario bizScenario){
        if(bizScenario == null || StringUtils.isEmpty(bizScenario.getUniqueIdentity())){
            tacResult.getBackupMetaData().setUseBackup(true);
            return tacResult;
        }
        if(tacResult == null || CollectionUtils.isEmpty(tacResult.getData())){
            tacResult = TacResult.errorResult("TacResultBackup");

            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                .kv("key",TAC_BACKUP_KEY)
                .kv("tacResultBackup","true")
                .info();
        }else{
            HadesLogUtil.stream(bizScenario.getUniqueIdentity())
                .kv("key",TAC_BACKUP_KEY)
                .kv("tacResultBackup","false")
                .info();
        }
        tacResult.getBackupMetaData().setUseBackup(true);
        return tacResult;
    }

}
