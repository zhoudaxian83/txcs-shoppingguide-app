package com.tmall.wireless.tac.biz.processor.detail.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.tmall.tcls.gs.sdk.framework.model.ContentVO;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: guichen
 * @Data: 2021/9/10
 * @Description:
 */
@Data
public class DetailRecommendVO extends ContentVO {

    private List<DetailTextComponentVO> title;
    private List<DetailTextComponentVO> subTitle;
    private String img;
    private List<DetailLabelVO> imgIconList;
    private List<DetailLabelVO> promotionAtmosphereList;
    private String titleMaxLine="2";
    private String type;

    private List<DetailEvent> event;


    @Data
    @NoArgsConstructor
    public class DetailLabelVO {
        private String title;
        private String text;
        private String url;
        private String bgUrl;
        private String textColor;
        private String bgColor;//背景色
        private String borderColor;//边框颜色
        private String cornerRadius; //圆角
    }

    @Data
    public static class DetailEvent implements Serializable {
        /**
         * 事件类型
         */
        private String type;
        /**
         * 事件的参数字段
         */
        private Map<String, Object> fields;



        public DetailEvent(String type){
            this.type=type;
        }

        /**
         * 这个方法名单词拼写错误
         * 当前引用的地方太多 只能将错就错
         * @param key
         * @param params
         */
        public void addFiledsParam(String key, Object params) {
            if (null == fields) {
                fields = Maps.newLinkedHashMap();
            }
            fields.put(key, params);
        }

        public DetailEvent addArgs(String key, Object value) {
            if ( null == fields) {
                addFiledsParam("args", new HashMap<>());
            }
            Map<String, Object> argsMap = (Map<String, Object>) fields.get("args");
            if (null == argsMap) {
                argsMap = Maps.newLinkedHashMap();
                fields.put("args", argsMap);
            }
            argsMap.put(key, value);
            return this;
        }
    }


}
