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
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by yangqing.byq on 2021/1/24.
 */

public class AppColaBootstrap implements BeanPostProcessor, ApplicationContextAware {

    @Autowired
    public List<ExtensionPointI> appExtPts;
    @Autowired
    private ExtensionRepository extensionRepository;

    public static ApplicationContext applicationContext;



    public void init() {
        Map<String, ExtensionPointI> beansOfType =
                applicationContext.getBeansOfType(ExtensionPointI.class);
        if (MapUtils.isEmpty(beansOfType)) {

        }
        if (CollectionUtils.isNotEmpty(appExtPts)) {
            registerBeans(appExtPts.stream().map(Object::getClass).collect(Collectors.toSet()));
        }
    }

    /**
     * @param classSet
     */
    private void registerBeans(Set<Class<?>> classSet) {


        for (Class<?> targetClz : classSet) {

            Extension extensionAnn = targetClz.getDeclaredAnnotation(Extension.class);
            if (extensionAnn != null) {
                doRegistration(targetClz);
            }

        }

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


    public void doRegistration(Class<?> targetClz) {
        ExtensionPointI extension = (ExtensionPointI) AppColaBootstrap.getBean(targetClz);
        Extension extensionAnn = targetClz.getDeclaredAnnotation(Extension.class);
        String extPtClassName = calculateExtensionPoint(targetClz);
        BizScenario bizScenario = BizScenario.valueOf(extensionAnn.bizId(), extensionAnn.useCase(), extensionAnn.scenario());
        ExtensionCoordinate extensionCoordinate = new ExtensionCoordinate(extPtClassName, bizScenario.getUniqueIdentity());
        ExtensionPointI preVal = extensionRepository.getExtensionRepo().put(extensionCoordinate, extension);
        if (preVal != null) {
            throw new ColaException("Duplicate registration is not allowed for :" + extensionCoordinate);
        }
    }

    private String calculateExtensionPoint(Class<?> targetClz) {
        Class[] interfaces = targetClz.getInterfaces();
        if (ArrayUtils.isEmpty(interfaces))
            throw new ColaException("Please assign a extension point interface for "+targetClz);
        for (Class intf : interfaces) {
            String extensionPoint = intf.getSimpleName();
            if (StringUtils.contains(extensionPoint, ColaConstant.EXTENSION_EXTPT_NAMING))
                return intf.getName();
        }
        throw new ColaException("Your name of ExtensionPoint for "+targetClz+" is not valid, must be end of "+ ColaConstant.EXTENSION_EXTPT_NAMING);
    }

}
