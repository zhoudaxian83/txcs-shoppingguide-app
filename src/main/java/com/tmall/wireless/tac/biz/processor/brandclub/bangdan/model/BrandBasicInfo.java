package com.tmall.wireless.tac.biz.processor.brandclub.bangdan.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class BrandBasicInfo {
    /**
     * 品牌ID
     */
    private String brandId;
    /**
     * 品牌名称
     */
    private String brandName;
    /**
     * 品牌名称，小二配置，专用于还没开通品牌馆的品牌
     */
    private String brandNameAdmin;
    /**
     * 品牌馆logo，商家上传的
     */
    private String brandLogo;
    /**
     * 品牌logo，小二上传，专用于还没开通品牌馆的品牌
     */
    private String brandLogoAdmin;
    /**
     * 品牌馆首页跳转链接
     */
    private String homePageUrl;
    /**
     * 品牌馆上线状态
     * 0-初始化
     * 1-已上线
     */
    private Integer status;

    /**
     * 品牌馆关联的公司,多个为逗号分格（不区分中英文逗号）
     */
    private String dataUser;

    /**
     * 获取品牌馆关联的公司
     */
    public List<String> associatedCompany() {
        if(StringUtils.isBlank(dataUser)) {
            return new ArrayList<>();
        }
        return Arrays.asList(dataUser.split("，|,"));
    }

    /**
     * 获取品牌logo，商家上传logo优先，没有的话使用小二上传的
     * @return
     */
    public String logo() {
        return StringUtils.isNotBlank(this.brandLogo) ? this.brandLogo : this.brandLogoAdmin;
    }

    /**
     * 获取品牌名称，商家配置的优先，没有的话使用小二配置的
     * @return
     */
    public String name() {
        return StringUtils.isNotBlank(this.brandName) ? this.brandName : this.brandNameAdmin;
    }

    public static void main(String[] args) {
        String str = "A公司，B公司,C公司,D公司";
        List<String> strings = Arrays.asList(str.split("，|,"));
        System.out.println("strings = " + strings);

    }
}
