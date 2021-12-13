package com.tmall.wireless.tac.biz.processor.processtemplate.common.service;

import com.tmall.aselfcaptain.item.model.ItemDTO;
import com.tmall.wireless.tac.biz.processor.processtemplate.common.ProcessTemplateContext;

import java.util.List;
import java.util.Map;

public interface ProcessTemplateRenderService {
    Map<Long, ItemDTO> batchQueryItem(List<Long> itemIdList, ProcessTemplateContext processTemplateContext);
}
