package com.tmall.wireless.tac.biz.processor.brandclub.bangdan.model;

public class FuzzyUtil {

    public static void main(String[] args) {
        System.out.println("fuzzy(0) = " + fuzzy(0));
        System.out.println("fuzzy(83) = " + fuzzy(8));
        System.out.println("fuzzy(100) = " + fuzzy(100));
        System.out.println("fuzzy(301) = " + fuzzy(301));
        System.out.println("fuzzy(9877) = " + fuzzy(9877));
        System.out.println("fuzzy(100003) = " + fuzzy(100003));
        System.out.println("fuzzy(3002345) = " + fuzzy(3002345));
    }
    /**
     * 模糊化数字，规则如下：
     * 区间：1-100展示内容：准确数字，1,2,3,……,100
     * 区间：101-1000展示内容：模糊化每层100递增，100+,200+,300+,……,900+
     * 区间：1001-1w展示内容：模糊化每层1000递增，1000+,2000+,3000+,……,9000+
     * 区间：10001-10万展示内容：模糊化每层1w递增，1万+,2万+,3万+,……,9万+
     * 区间：100001-100万展示内容：模糊化每层10万递增，10万+,20万+,30万+,……,90万+
     * 区间：100万以上展示内容：100万+
     * @param num 待模糊化数字
     * @return
     */
    public static String fuzzy(Integer num) {
        if(num == null) {
            return "";
        }
        if(num <=100) {
            return String.valueOf(num);
        }
        if(num > 100 && num <=1000) {
            return (num - 1) / 100 + "00+";
        }
        if(num > 1000 && num <= 10000) {
            return (num - 1) / 1000 + "000+";
        }
        if(num > 10000 && num <= 100000) {
            return (num - 1) / 10000 + "万+";
        }
        if(num > 100000 && num <= 1000000) {
            return (num - 1) / 100000 + "0万+";
        }
        return "100万+";
    }
}
