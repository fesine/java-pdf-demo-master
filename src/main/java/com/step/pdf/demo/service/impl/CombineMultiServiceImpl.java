package com.step.pdf.demo.service.impl;

import com.step.pdf.demo.multiconfig.annotation.MultiService;
import com.step.pdf.demo.service.CombineMultiService;
import com.step.pdf.demo.service.NormalService;
import com.step.pdf.demo.service.RedisMultiService;

import javax.annotation.Resource;

/**
 * @description: 组合服务里面调用多实例服务，服务本身也是多实例(iis,jw)
 * @author: fesine
 * @createTime:2021/9/18
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/18
 */
@MultiService
public class CombineMultiServiceImpl implements CombineMultiService {

    @MultiService(name = "redis")
    private RedisMultiService redisMultiService;

    @Resource
    private NormalService normalService;

    @Override
    public String handler(String msg) {
        redisMultiService.redis();
        normalService.normal();
        return "combine handle :"+msg;
    }
}
