package com.tmall.wireless.tac.biz.processor.alipay.service.impl.atomic;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AtomicCardProcessorFactory implements InitializingBean {

    @Autowired
    List<IAtomicCardProcessor> atomicCardProcessors;

    Map<String, IAtomicCardProcessor> floorProcessorMap = Maps.newHashMap();
    @Override
    public void afterPropertiesSet() throws Exception {
        for (IAtomicCardProcessor iFloorProcessor : atomicCardProcessors) {
            floorProcessorMap.put(iFloorProcessor.atomicCardId(), iFloorProcessor);
        }
    }

    public IAtomicCardProcessor getProcessorByFloorId(String floorId) {
        return floorProcessorMap.get(floorId);
    }
}
