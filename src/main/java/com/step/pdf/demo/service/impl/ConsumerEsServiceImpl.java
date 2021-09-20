package com.step.pdf.demo.service.impl;

import com.step.pdf.demo.multiconfig.annotation.MultiService;
import com.step.pdf.demo.multiconfig.annotation.ServiceGroup;
import com.step.pdf.demo.service.ConsumerService;
import com.step.pdf.demo.service.EsMultiService;
import org.springframework.stereotype.Service;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/20
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/20
 */
@Service("esService")
@ServiceGroup("iis")
public class ConsumerEsServiceImpl implements ConsumerService {

    @MultiService(name = "esMultiService")
    private EsMultiService esMultiService;

    @Override
    public String consumer(String msg) {
        esMultiService.es();
        return "consumer:"+msg;
    }
}
