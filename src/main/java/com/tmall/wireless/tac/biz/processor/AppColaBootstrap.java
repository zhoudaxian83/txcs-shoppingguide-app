package com.tmall.wireless.tac.biz.processor;

import com.alibaba.cola.boot.*;
import com.alibaba.cola.common.ApplicationContextHelper;
import com.alibaba.cola.common.ColaConstant;
import com.alibaba.cola.exception.SysException;
import com.alibaba.cola.exception.framework.BasicErrorCode;
import com.alibaba.cola.exception.framework.ColaException;
import com.alibaba.cola.extension.*;
import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by yangqing.byq on 2021/1/24.
 */

public class AppColaBootstrap implements BeanPostProcessor, ApplicationContextAware {

    @Autowired
    public List<ExtensionPointI> appExtPts;

    private static ApplicationContext applicationContext;
    @Getter
    @Setter
    private List<String> packages;
    private ClassPathScanHandler handler;

    @Autowired
    private RegisterFactory registerFactory;



    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        init();
        return bean;
    }

    public void init() {
        Set<String> collect = appExtPts.stream().map(pt -> pt.getClass().getName() + " " +
                pt.getClass().getClassLoader().getClass().getName()).collect(Collectors.toSet());

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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        AppColaBootstrap.applicationContext = applicationContext;
    }

    public static<T> T getBean(Class<T> targetClz){
        T beanInstance = null;
        //优先按type查
        try {
            beanInstance = (T) applicationContext.getBean(targetClz);
        }catch (Exception e){
        }
        //按name查
        if(beanInstance == null){
            String simpleName = targetClz.getSimpleName();
            //首字母小写
            simpleName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
            beanInstance = (T) applicationContext.getBean(simpleName);
        }
        if(beanInstance == null){
            new SysException(BasicErrorCode.COLA_ERROR, "Component " + targetClz + " can not be found in Spring Container");
        }
        return beanInstance;
    }
    public static Object getBean(String claz){
        return AppColaBootstrap.applicationContext.getBean(claz);
    }


}
