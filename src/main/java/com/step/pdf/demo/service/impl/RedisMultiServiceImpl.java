package com.step.pdf.demo.service.impl;

import com.step.pdf.demo.config.RedisConfig;
import com.step.pdf.demo.multiconfig.annotation.MultiService;
import com.step.pdf.demo.service.RedisMultiService;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/19
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/19
 */
@MultiService
public class RedisMultiServiceImpl implements RedisMultiService {


    @MultiService
    private RedisConfig redisConfig;

    @Override
    public void redis() {
        System.out.println("------>" + redisConfig.toString());
    }
}
