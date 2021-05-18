package com.tmall.wireless.tac.biz.processor.todaycrazy;

import java.util.Map;
import com.alibaba.cola.extension.Extension;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataDTO;
import com.tmall.txcs.gs.framework.extensions.origindata.OriginDataItemQueryExtPt;
import com.tmall.txcs.gs.framework.model.SgFrameworkContextItem;
import com.tmall.txcs.gs.model.model.dto.ItemEntity;
import com.tmall.txcs.gs.spi.recommend.TairFactorySpi;
import com.tmall.wireless.tac.biz.processor.common.ScenarioConstantApp;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.MapSortUtil;
import com.tmall.wireless.tac.biz.processor.todaycrazy.utils.TairUtil;
import com.tmall.wireless.tac.client.dataservice.TacLogger;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author guijian
 */
@Extension(bizId = ScenarioConstantApp.BIZ_TYPE_SUPERMARKET,
    useCase = ScenarioConstantApp.LOC_TYPE_B2C,
    scenario = ScenarioConstantApp.SCENARIO_TODAY_CRAZY_LIMIT_TIME_BUY)
public class LimitTimeOriginDataItemQueryExtPt implements OriginDataItemQueryExtPt {

    @Autowired
    TairFactorySpi tairFactorySpi;

    @Autowired
    TacLogger tacLogger;

    private static final int NAME_SPACE = 184;
    public static final String ALD_CONTEXT = "aldContext";


    @Override
    public Flowable<OriginDataDTO<ItemEntity>> process(SgFrameworkContextItem sgFrameworkContextItem) {
        tacLogger.info("****LimitTimeOriginDataItemQueryExtPt sgFrameworkContextItem***"+sgFrameworkContextItem);
        tacLogger.info("****LimitTimeOriginDataItemQueryExtPt sgFrameworkContextItem***"+sgFrameworkContextItem.toString());
        tacLogger.info("****LimitTimeOriginDataItemQueryExtPt sgFrameworkContextItem***"+sgFrameworkContextItem.getRequestParams());
        Map<String, Object> params = sgFrameworkContextItem.getRequestParams();
        //第几个时间段
        /*String index = params.get("index");*/
        //ald排期信息
        Map<String, Object> contextObj = (Map<String, Object>)params.get(ALD_CONTEXT);

        /*return (List<Map<String, Object>>)contextObj.get("static_schedule_data");*/

        DataEntry dataEntry = getCacheData();



        return null;
    }

    /**
     * 获取缓存数据
     * @return
     */
    private DataEntry getCacheData(){
        DataEntry dataEntry = new DataEntry();
        try {
            String normalTairKey = TairUtil.formatHotTairKey();
            Result<DataEntry> rst = tairFactorySpi.getOriginDataFailProcessTair().getMultiClusterTairManager().get(NAME_SPACE,normalTairKey);
            if (rst.isSuccess() && rst.getRc() != null && rst.getRc().isSuccess()) {
                dataEntry = rst.getValue();
            }
        }catch (Exception e){
            tacLogger.error("", e);
        }
        return dataEntry;
    }

    /**
     * 时间段排序
     * @return
     */
    public Map<String,String> sortMap(Map<String,String> map){
        Map<String,String> rsMap = MapSortUtil.sortMapByValue(map);

        return rsMap;
    }

}
