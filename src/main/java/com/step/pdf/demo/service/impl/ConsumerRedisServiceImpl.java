package com.step.pdf.demo.service.impl;

import com.step.pdf.demo.multiconfig.annotation.MultiService;
import com.step.pdf.demo.multiconfig.annotation.ServiceGroup;
import com.step.pdf.demo.service.ConsumerService;
import com.step.pdf.demo.service.RedisMultiService;
import org.springframework.stereotype.Service;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/20
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/20
 */
@Service("redisService")
@ServiceGroup("wuxi")
public class ConsumerRedisServiceImpl implements ConsumerService {

    @MultiService
    private RedisMultiService redisMultiService;

    @Override
    public String consumer(String msg) {
        redisMultiService.redis();
        return "consumer:"+msg;
    }
}
