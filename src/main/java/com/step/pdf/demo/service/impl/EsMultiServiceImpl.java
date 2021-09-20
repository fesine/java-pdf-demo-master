package com.step.pdf.demo.service.impl;

import com.step.pdf.demo.config.EsConfig;
import com.step.pdf.demo.multiconfig.annotation.MultiService;
import com.step.pdf.demo.service.EsMultiService;

/**
 * @description: 此服务会有两个iis和jw
 * @author: fesine
 * @createTime:2021/9/18
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/18
 */
@MultiService(name = "esMultiService")
public class EsMultiServiceImpl implements EsMultiService {

    @MultiService
    private EsConfig esConfig;

    @Override
    public void es() {
        System.out.println("------>"+esConfig.toString());
    }

}
