package com.tmall.wireless.tac.biz.processor.detail.model;

import java.io.Serializable;

import lombok.Data;

/**
 * @author: guichen
 * @Data: 2020/12/30
 * @Description:
 */
@Data
public class DetailTextComponentVO {

    private String text;
    private Style style;

    public DetailTextComponentVO(String text,Style style){
        this.text=text;
        this.style=style;
    }

    @Data
    public static class Style implements Serializable {
        private String size;
        private String color;
        private String tailIndent;
        private String strikeThrough;
        private String backgroundColor;
        private String italic;
        private String bold;

        public Style(String size, String color, String tailIndent) {
            this.size = size;
            this.color = color;
            this.tailIndent = tailIndent;
        }

        public Style(String size, String color, String tailIndent,String strikeThrough,String backgroundColor,String italic) {
            this.size = size;
            this.color = color;
            this.tailIndent = tailIndent;
            this.strikeThrough=strikeThrough;
            this.backgroundColor=backgroundColor;
            this.italic=italic;
        }
    }
}
