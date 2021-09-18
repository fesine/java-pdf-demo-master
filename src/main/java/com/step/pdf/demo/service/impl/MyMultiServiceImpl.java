package com.step.pdf.demo.service.impl;

import com.step.pdf.demo.config.MyConfig;
import com.step.pdf.demo.multiconfig.annotation.MultiService;
import com.step.pdf.demo.service.MyMultiService;

import javax.annotation.Resource;

/**
 * @description: 此服务会有两个iis和jw
 * @author: fesine
 * @createTime:2021/9/18
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/18
 */
@MultiService
public class MyMultiServiceImpl implements MyMultiService {

    @MultiService
    @Resource
    private MyConfig myConfig;

    @Override
    public void sayHello() {
        System.out.println(myConfig.toString());
    }
}
