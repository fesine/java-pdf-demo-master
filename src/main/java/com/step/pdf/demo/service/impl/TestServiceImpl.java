package com.step.pdf.demo.service.impl;

import com.step.pdf.demo.config.RedisConfig;
import com.step.pdf.demo.multiconfig.annotation.MultiService;
import com.step.pdf.demo.service.TestService;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/19
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/19
 */
@MultiService(group = {"wuxi"})
public class TestServiceImpl implements TestService {


    @MultiService
    private RedisConfig redisConfig;

    @Override
    public void test() {
        redisConfig.toString();
    }
}
