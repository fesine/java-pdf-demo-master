package com.step.pdf.demo.service.impl;

import com.step.pdf.demo.multiconfig.annotation.MultiService;
import com.step.pdf.demo.service.CombineMultiService;
import com.step.pdf.demo.service.TestService;

/**
 * @description: 组合服务里面调用多实例服务，服务本身也是多实例(iis,jw)
 * @author: fesine
 * @createTime:2021/9/18
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/18
 */
@MultiService(group = "wuxi")
public class CombineMultiServiceImpl implements CombineMultiService {

    @MultiService
    private TestService testService;

    @Override
    public void handler() {
        testService.test();
    }
}
