package com.tmall.wireless.tac.biz.processor;

import com.alibaba.cola.boot.*;
import com.alibaba.cola.exception.framework.ColaException;
import com.alibaba.cola.extension.ExtensionPointI;
import com.alibaba.fastjson.JSON;
import com.tmall.recommend.biz.RpmOptLogger;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by yangqing.byq on 2021/1/24.
 */

public class AppColaBootstrap {


    @Autowired
    List<ExtensionPointI> appExtPts;
//    @Autowired
//    AppRanderExtPt appRanderExtPt;
    @Getter
    @Setter
    private List<String> packages;
    private ClassPathScanHandler handler;

    @Autowired
    private RegisterFactory registerFactory;
    @Autowired
    private RpmOptLogger rpmOptLogger;

    public void init() {
//        Set<Class<?>> classSet = scanConfiguredPackages();
//        registerBeans(classSet);
//        registerBeans(Sets.newHashSet(appRanderExtPt.getClass()));

        Set<String> collect = appExtPts.stream().map(pt -> pt.getClass().getName() + " " +
                pt.getClass().getClassLoader().getClass().getName()).collect(Collectors.toSet());

        rpmOptLogger.error("appExtPts:" + JSON.toJSONString(collect));

        if (CollectionUtils.isNotEmpty(appExtPts)) {
            registerBeans(appExtPts.stream().map(Object::getClass).collect(Collectors.toSet()));
        }
    }

    /**
     * @param classSet
     */
    private void registerBeans(Set<Class<?>> classSet) {
        for (Class<?> targetClz : classSet) {
            RegisterI register = registerFactory.getRegister(targetClz);
            if (null != register) {
                register.doRegistration(targetClz);
            }
        }

    }

    /**
     * Scan the packages configured in Spring xml
     *
     * @return
     */
    private Set<Class<?>> scanConfiguredPackages() {
        if (packages == null) throw new ColaException("Command packages is not specified");

        String[] pkgs = new String[packages.size()];
        handler = new ClassPathScanHandler(packages.toArray(pkgs));

        Set<Class<?>> classSet = new TreeSet<>(new ClassNameComparator());
        for (String pakName : packages) {
            classSet.addAll(handler.getPackageAllClasses(pakName, true));
        }
        return classSet;
    }
}